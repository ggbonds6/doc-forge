package com.docforge.govdoc.template;

import com.docforge.govdoc.common.ApiException;
import com.docforge.govdoc.common.JsonCodec;
import com.docforge.govdoc.document.DocumentType;
import com.docforge.govdoc.render.DocxRenderService;
import com.docforge.govdoc.render.FileStorageService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class TemplateService {
    private static final String ACCEPTANCE_PENDING = "PENDING";
    private static final String ACCEPTANCE_PASS = "PASS";
    private static final String ACCEPTANCE_FAIL = "FAIL";
    private static final String DEFAULT_REVIEWER = "默认管理员";

    private final TemplatePackRepository templatePackRepository;
    private final TemplateVersionRepository templateVersionRepository;
    private final JsonCodec jsonCodec;
    private final FileStorageService fileStorageService;
    private final DocxRenderService docxRenderService;

    public TemplateService(
            TemplatePackRepository templatePackRepository,
            TemplateVersionRepository templateVersionRepository,
            JsonCodec jsonCodec,
            FileStorageService fileStorageService,
            DocxRenderService docxRenderService
    ) {
        this.templatePackRepository = templatePackRepository;
        this.templateVersionRepository = templateVersionRepository;
        this.jsonCodec = jsonCodec;
        this.fileStorageService = fileStorageService;
        this.docxRenderService = docxRenderService;
    }

    @Transactional
    public void ensureBuiltIns() {
        for (TemplatePackCode code : TemplatePackCode.values()) {
            TemplatePackEntity pack = templatePackRepository.findByCode(code.name())
                    .orElseGet(() -> createPack(code));
            if (templateVersionRepository.findFirstByTemplatePackIdAndStatusOrderByUpdatedAtDesc(pack.getId(), TemplateStatus.PUBLISHED.name()).isEmpty()) {
                TemplateVersionEntity version = new TemplateVersionEntity();
                version.setId(UUID.randomUUID().toString());
                version.setTemplatePackId(pack.getId());
                version.setName(code.getDisplayName() + "标准模板");
                version.setDescription("内置标准模板包");
                version.setVersionLabel("v1");
                version.setStatus(TemplateStatus.PUBLISHED.name());
                version.setRuleProfileJson(jsonCodec.write(defaultProfile(code)));
                version.setPreviewBaselineJson(jsonCodec.write(defaultBaseline(code.getDocumentType(), null)));
                version.setCreatedAt(Instant.now());
                version.setUpdatedAt(Instant.now());
                version = templateVersionRepository.save(version);
                compileTemplate(version, code.getDocumentType());
            }
        }
    }

    public List<TemplatePackSummary> listTemplatePacks() {
        return templatePackRepository.findAll().stream()
                .sorted(Comparator.comparing(TemplatePackEntity::getCode))
                .map(pack -> {
                    List<TemplateVersionSummary> versions = templateVersionRepository.findByTemplatePackIdOrderByCreatedAtDesc(pack.getId())
                            .stream()
                            .map(this::toSummary)
                            .toList();
                    return new TemplatePackSummary(pack.getId(), pack.getCode(), pack.getName(), pack.getDescription(), pack.isBuiltIn(), versions);
                })
                .toList();
    }

    public TemplateVersionDetail getTemplateVersion(String id) {
        return toDetail(findTemplateVersion(id));
    }

    @Transactional
    public TemplateVersionDetail createDraft(CreateDraftRequest request) {
        TemplatePackEntity pack = templatePackRepository.findByCode(request.packCode())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "模板包不存在"));
        TemplateVersionEntity source = request.sourceTemplateVersionId() != null
                ? findTemplateVersion(request.sourceTemplateVersionId())
                : templateVersionRepository.findFirstByTemplatePackIdAndStatusOrderByUpdatedAtDesc(pack.getId(), TemplateStatus.PUBLISHED.name())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "未找到可复制的模板版本"));

        TemplateVersionEntity entity = new TemplateVersionEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setTemplatePackId(pack.getId());
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setVersionLabel("draft-" + Instant.now().toEpochMilli());
        entity.setStatus(TemplateStatus.DRAFT.name());
        entity.setRuleProfileJson(source.getRuleProfileJson());
        DocumentType documentType = TemplatePackCode.valueOf(pack.getCode()).getDocumentType();
        entity.setPreviewBaselineJson(jsonCodec.write(resetBaselineReview(readBaseline(source.getPreviewBaselineJson(), documentType), documentType)));
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        return toDetail(templateVersionRepository.save(entity));
    }

    @Transactional
    public TemplateVersionDetail updateDraft(String id, UpdateTemplateRequest request) {
        TemplateVersionEntity entity = findTemplateVersion(id);
        if (!TemplateStatus.DRAFT.name().equals(entity.getStatus())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "仅草稿模板可编辑");
        }
        if (request.ruleProfile() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "模板规则不能为空");
        }
        String nextRuleProfileJson = jsonCodec.write(request.ruleProfile());
        boolean ruleChanged = !nextRuleProfileJson.equals(entity.getRuleProfileJson());
        entity.setName(request.name());
        entity.setDescription(request.description());
        entity.setRuleProfileJson(nextRuleProfileJson);
        if (ruleChanged) {
            DocumentType documentType = documentTypeFor(entity);
            entity.setPreviewBaselineJson(jsonCodec.write(resetBaselineReview(readBaseline(entity.getPreviewBaselineJson(), documentType), documentType)));
        }
        entity.setUpdatedAt(Instant.now());
        return toDetail(templateVersionRepository.save(entity));
    }

    @Transactional
    public TemplateVersionDetail publish(String id, PublishTemplateRequest request) {
        TemplateVersionEntity entity = findTemplateVersion(id);
        if (!TemplateStatus.DRAFT.name().equals(entity.getStatus())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "仅草稿模板可发布");
        }
        DocumentType documentType = documentTypeFor(entity);
        TemplatePreviewBaseline baseline = readBaseline(entity.getPreviewBaselineJson(), documentType);
        List<TemplateAcceptanceCriterion> notAcceptedBlockingCriteria = baseline.getCriteria().stream()
                .filter(TemplateAcceptanceCriterion::isBlocking)
                .filter(item -> !ACCEPTANCE_PASS.equals(item.getStatus()))
                .toList();
        if (!notAcceptedBlockingCriteria.isEmpty()) {
            String overrideReason = request == null ? null : trimToNull(request.overrideReason());
            if (overrideReason == null) {
                String names = notAcceptedBlockingCriteria.stream()
                        .map(TemplateAcceptanceCriterion::getName)
                        .limit(3)
                        .reduce((left, right) -> left + "、" + right)
                        .orElse("阻断项");
                throw new ApiException(HttpStatus.BAD_REQUEST, "模板验收未通过，阻断项需先标记为 PASS：" + names);
            }
            baseline.setReviewStatus("OVERRIDE_PUBLISHED");
            baseline.setPublishOverrideReason(overrideReason);
            baseline.setPublishOverrideReviewer(defaultReviewer(request.overrideReviewer()));
        } else {
            baseline.setReviewStatus("PUBLISHED_ACCEPTED");
            baseline.setPublishOverrideReason(null);
            baseline.setPublishOverrideReviewer(null);
        }
        baseline.setPublishedAt(Instant.now());
        entity.setPreviewBaselineJson(jsonCodec.write(baseline));

        templateVersionRepository.findByTemplatePackIdOrderByCreatedAtDesc(entity.getTemplatePackId()).stream()
                .filter(item -> TemplateStatus.PUBLISHED.name().equals(item.getStatus()))
                .forEach(item -> {
                    item.setStatus(TemplateStatus.ARCHIVED.name());
                    item.setUpdatedAt(Instant.now());
                    templateVersionRepository.save(item);
                });
        entity.setStatus(TemplateStatus.PUBLISHED.name());
        entity.setVersionLabel("v" + Instant.now().toEpochMilli());
        entity.setUpdatedAt(Instant.now());
        entity = templateVersionRepository.save(entity);
        compileTemplate(entity, documentType);
        return toDetail(entity);
    }

    @Transactional
    public TemplateVersionDetail reviewAcceptance(String id, AcceptanceReviewRequest request) {
        TemplateVersionEntity entity = findTemplateVersion(id);
        if (!TemplateStatus.DRAFT.name().equals(entity.getStatus())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "仅草稿模板可执行验收");
        }
        if (request == null || trimToNull(request.code()) == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "验收项编码不能为空");
        }
        String status = trimToNull(request.status());
        if (!ACCEPTANCE_PASS.equals(status) && !ACCEPTANCE_FAIL.equals(status)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "验收状态仅支持 PASS 或 FAIL");
        }

        DocumentType documentType = documentTypeFor(entity);
        TemplatePreviewBaseline baseline = readBaseline(entity.getPreviewBaselineJson(), documentType);
        TemplateAcceptanceCriterion criterion = baseline.getCriteria().stream()
                .filter(item -> request.code().equals(item.getCode()))
                .findFirst()
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "验收项不存在"));
        criterion.setStatus(status);
        criterion.setReviewer(defaultReviewer(request.reviewer()));
        criterion.setComment(request.comment());
        criterion.setReviewedAt(Instant.now());
        baseline.setReviewStatus(resolveReviewStatus(baseline));
        entity.setPreviewBaselineJson(jsonCodec.write(baseline));
        entity.setUpdatedAt(Instant.now());
        return toDetail(templateVersionRepository.save(entity));
    }

    @Transactional
    public TemplateVersionDetail createDraftFromRule(String sourceTemplateVersionId, String name, String description, TemplateRuleProfile ruleProfile) {
        TemplateVersionEntity source = findTemplateVersion(sourceTemplateVersionId);
        TemplatePackEntity pack = templatePackRepository.findById(source.getTemplatePackId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "模板包不存在"));
        TemplateVersionEntity entity = new TemplateVersionEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setTemplatePackId(pack.getId());
        entity.setName(name);
        entity.setDescription(description);
        entity.setVersionLabel("draft-" + Instant.now().toEpochMilli());
        entity.setStatus(TemplateStatus.DRAFT.name());
        entity.setRuleProfileJson(jsonCodec.write(ruleProfile));
        DocumentType documentType = TemplatePackCode.valueOf(pack.getCode()).getDocumentType();
        entity.setPreviewBaselineJson(jsonCodec.write(resetBaselineReview(readBaseline(source.getPreviewBaselineJson(), documentType), documentType)));
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        return toDetail(templateVersionRepository.save(entity));
    }

    public TemplateRuleProfile getRuleProfile(String templateVersionId) {
        return jsonCodec.read(findTemplateVersion(templateVersionId).getRuleProfileJson(), TemplateRuleProfile.class);
    }

    public TemplateRuleProfile mergeRuleProfile(String templateVersionId, TemplateRuleProfile overrideProfile) {
        TemplateRuleProfile base = getRuleProfile(templateVersionId);
        if (overrideProfile == null) {
            return base;
        }
        if (overrideProfile.getPage() != null) {
            base.setPage(overrideProfile.getPage());
        }
        if (overrideProfile.getTitleStyle() != null) {
            base.setTitleStyle(overrideProfile.getTitleStyle());
        }
        if (overrideProfile.getHeading1Style() != null) {
            base.setHeading1Style(overrideProfile.getHeading1Style());
        }
        if (overrideProfile.getHeading2Style() != null) {
            base.setHeading2Style(overrideProfile.getHeading2Style());
        }
        if (overrideProfile.getHeading3Style() != null) {
            base.setHeading3Style(overrideProfile.getHeading3Style());
        }
        if (overrideProfile.getParagraphStyle() != null) {
            base.setParagraphStyle(overrideProfile.getParagraphStyle());
        }
        if (overrideProfile.getTableStyle() != null) {
            base.setTableStyle(overrideProfile.getTableStyle());
        }
        if (overrideProfile.getHeaderFooter() != null) {
            base.setHeaderFooter(overrideProfile.getHeaderFooter());
        }
        if (overrideProfile.getCompliance() != null) {
            base.setCompliance(overrideProfile.getCompliance());
        }
        return base;
    }

    private void compileTemplate(TemplateVersionEntity entity, DocumentType documentType) {
        try {
            byte[] bytes = docxRenderService.renderTemplatePreview(documentType, jsonCodec.read(entity.getRuleProfileJson(), TemplateRuleProfile.class));
            entity.setCompiledTemplatePath(fileStorageService.writeTemplateFile(entity.getId(), bytes));
            TemplatePreviewBaseline baseline = readBaseline(entity.getPreviewBaselineJson(), documentType);
            baseline.setCompiledTemplatePath(entity.getCompiledTemplatePath());
            baseline.setCompiledAt(Instant.now());
            baseline.setDocumentType(documentType.name());
            entity.setPreviewBaselineJson(jsonCodec.write(baseline));
            entity.setUpdatedAt(Instant.now());
            templateVersionRepository.save(entity);
        } catch (IOException exception) {
            throw new IllegalStateException("模板编译失败", exception);
        }
    }

    private TemplatePackEntity createPack(TemplatePackCode code) {
        TemplatePackEntity pack = new TemplatePackEntity();
        pack.setId(UUID.randomUUID().toString());
        pack.setCode(code.name());
        pack.setName(code.getDisplayName());
        pack.setDescription("内置" + code.getDisplayName() + "模板包");
        pack.setBuiltIn(true);
        pack.setCreatedAt(Instant.now());
        pack.setUpdatedAt(Instant.now());
        return templatePackRepository.save(pack);
    }

    private TemplateRuleProfile defaultProfile(TemplatePackCode code) {
        TemplateRuleProfile profile = new TemplateRuleProfile();
        profile.getTitleStyle().setFontFamily("方正小标宋简体");
        profile.getTitleStyle().setFontSize(22);
        profile.getTitleStyle().setAlign("center");
        profile.getTitleStyle().setSpacingBefore(240);
        profile.getTitleStyle().setSpacingAfter(240);
        profile.getHeading1Style().setFontFamily("黑体");
        profile.getHeading1Style().setFontSize(16);
        profile.getHeading1Style().setBold(true);
        profile.getHeading1Style().setAlign("left");
        profile.getHeading2Style().setFontFamily("楷体");
        profile.getHeading2Style().setFontSize(16);
        profile.getHeading2Style().setBold(true);
        profile.getHeading3Style().setFontFamily("仿宋");
        profile.getHeading3Style().setFontSize(16);
        profile.getHeading3Style().setBold(true);
        profile.getParagraphStyle().setFontFamily("仿宋");
        profile.getParagraphStyle().setFontSize(16);
        profile.getParagraphStyle().setLineHeight(1.5);
        profile.getParagraphStyle().setFirstLineIndent("2em");
        profile.getCompliance().setAllowedDocTypes(List.of(code.getDocumentType()));
        return profile;
    }

    private TemplatePreviewBaseline readBaseline(String baselineJson, DocumentType documentType) {
        TemplatePreviewBaseline baseline = jsonCodec.read(baselineJson, TemplatePreviewBaseline.class);
        if (baseline == null || baseline.getCriteria() == null || baseline.getCriteria().isEmpty()) {
            return defaultBaseline(documentType, null);
        }
        return baseline;
    }

    private TemplatePreviewBaseline resetBaselineReview(TemplatePreviewBaseline baseline, DocumentType documentType) {
        TemplatePreviewBaseline next = baseline == null ? defaultBaseline(documentType, null) : baseline;
        next.setDocumentType(documentType.name());
        next.setCompiledTemplatePath(null);
        next.setCompiledAt(null);
        next.setReviewStatus("PENDING_WPS_REVIEW");
        next.setPublishOverrideReason(null);
        next.setPublishOverrideReviewer(null);
        next.setPublishedAt(null);
        if (next.getCriteria() == null || next.getCriteria().isEmpty()) {
            next.setCriteria(defaultBaseline(documentType, null).getCriteria());
        }
        next.getCriteria().forEach(item -> {
            item.setStatus(ACCEPTANCE_PENDING);
            item.setReviewer(null);
            item.setComment(null);
            item.setReviewedAt(null);
        });
        return next;
    }

    private String resolveReviewStatus(TemplatePreviewBaseline baseline) {
        boolean hasBlockingFail = baseline.getCriteria().stream()
                .anyMatch(item -> item.isBlocking() && ACCEPTANCE_FAIL.equals(item.getStatus()));
        if (hasBlockingFail) {
            return "WPS_REVIEW_FAILED";
        }
        boolean hasBlockingPending = baseline.getCriteria().stream()
                .anyMatch(item -> item.isBlocking() && !ACCEPTANCE_PASS.equals(item.getStatus()));
        if (hasBlockingPending) {
            return "PENDING_WPS_REVIEW";
        }
        boolean hasOptionalFail = baseline.getCriteria().stream()
                .anyMatch(item -> !item.isBlocking() && ACCEPTANCE_FAIL.equals(item.getStatus()));
        if (hasOptionalFail) {
            return "READY_WITH_WARNINGS";
        }
        boolean hasOptionalPending = baseline.getCriteria().stream()
                .anyMatch(item -> !item.isBlocking() && !ACCEPTANCE_PASS.equals(item.getStatus()));
        return hasOptionalPending ? "READY_FOR_PUBLISH" : "ACCEPTED";
    }

    private String defaultReviewer(String reviewer) {
        String normalized = trimToNull(reviewer);
        return normalized == null ? DEFAULT_REVIEWER : normalized;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private TemplatePreviewBaseline defaultBaseline(DocumentType documentType, String compiledTemplatePath) {
        TemplatePreviewBaseline baseline = new TemplatePreviewBaseline();
        baseline.setDocumentType(documentType.name());
        baseline.setCompiledTemplatePath(compiledTemplatePath);
        baseline.setCriteria(List.of(
                new TemplateAcceptanceCriterion("page.margin", "页面与页边距", "A4 纵向页面，版心、上下左右边距与机构验收样例一致。", true),
                new TemplateAcceptanceCriterion("header.red", "版头与红色分隔线", "发文机关标志、红色分隔线、发文字号区域在 WPS/Word 中位置稳定。", true),
                new TemplateAcceptanceCriterion("title.style", "标题样式", "标题字体、字号、居中、段前段后符合模板规则。", true),
                new TemplateAcceptanceCriterion("body.style", "正文样式", "正文仿宋三号、首行缩进、行距、段落间距符合模板规则。", true),
                new TemplateAcceptanceCriterion("heading.numbering", "正文层级序号", "一级、二级、三级标题序号与公文惯例一致。", true),
                new TemplateAcceptanceCriterion("attachment.footer", "附件与版记", "附件说明、署名日期、版记、页码在导出 DOCX 中完整可编辑。", true),
                new TemplateAcceptanceCriterion("wps.open", "WPS 打开验收", "导出 DOCX 可用 WPS 打开、保存、继续编辑，无兼容性报错。", true)
        ));
        return baseline;
    }

    private TemplateVersionEntity findTemplateVersion(String id) {
        return templateVersionRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "模板版本不存在"));
    }

    private TemplateVersionSummary toSummary(TemplateVersionEntity entity) {
        return new TemplateVersionSummary(entity.getId(), entity.getName(), entity.getDescription(), entity.getVersionLabel(), entity.getStatus(), entity.getCompiledTemplatePath(), entity.getUpdatedAt());
    }

    private TemplateVersionDetail toDetail(TemplateVersionEntity entity) {
        DocumentType documentType = documentTypeFor(entity);
        return new TemplateVersionDetail(
                entity.getId(),
                entity.getTemplatePackId(),
                entity.getName(),
                entity.getDescription(),
                entity.getVersionLabel(),
                entity.getStatus(),
                jsonCodec.read(entity.getRuleProfileJson(), TemplateRuleProfile.class),
                readBaseline(entity.getPreviewBaselineJson(), documentType),
                entity.getCompiledTemplatePath(),
                entity.getUpdatedAt()
        );
    }

    private DocumentType documentTypeFor(TemplateVersionEntity entity) {
        TemplatePackEntity pack = templatePackRepository.findById(entity.getTemplatePackId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "模板包不存在"));
        return TemplatePackCode.valueOf(pack.getCode()).getDocumentType();
    }

    public record TemplatePackSummary(
            String id,
            String code,
            String name,
            String description,
            boolean builtIn,
            List<TemplateVersionSummary> versions
    ) {
    }

    public record TemplateVersionSummary(
            String id,
            String name,
            String description,
            String versionLabel,
            String status,
            String compiledTemplatePath,
            Instant updatedAt
    ) {
    }

    public record TemplateVersionDetail(
            String id,
            String templatePackId,
            String name,
            String description,
            String versionLabel,
            String status,
            TemplateRuleProfile ruleProfile,
            TemplatePreviewBaseline previewBaseline,
            String compiledTemplatePath,
            Instant updatedAt
    ) {
    }

    public record CreateDraftRequest(String packCode, String sourceTemplateVersionId, String name, String description) {
    }

    public record UpdateTemplateRequest(String name, String description, TemplateRuleProfile ruleProfile) {
    }

    public record AcceptanceReviewRequest(String code, String status, String reviewer, String comment) {
    }

    public record PublishTemplateRequest(String overrideReason, String overrideReviewer) {
    }
}
