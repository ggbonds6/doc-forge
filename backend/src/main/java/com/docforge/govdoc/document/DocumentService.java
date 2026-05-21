package com.docforge.govdoc.document;

import com.docforge.govdoc.common.ApiException;
import com.docforge.govdoc.common.JsonCodec;
import com.docforge.govdoc.render.DocxRenderService;
import com.docforge.govdoc.render.ExportJobEntity;
import com.docforge.govdoc.render.ExportJobRepository;
import com.docforge.govdoc.render.ExportJobStatus;
import com.docforge.govdoc.render.FileStorageService;
import com.docforge.govdoc.template.TemplateRuleProfile;
import com.docforge.govdoc.template.TemplateService;
import com.docforge.govdoc.validation.ComplianceEngine;
import com.docforge.govdoc.validation.ValidationReport;
import com.docforge.govdoc.validation.ValidationReportEntity;
import com.docforge.govdoc.validation.ValidationReportRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class DocumentService {
    private final GovDocumentRepository govDocumentRepository;
    private final GovDocumentVersionRepository govDocumentVersionRepository;
    private final ValidationReportRepository validationReportRepository;
    private final ExportJobRepository exportJobRepository;
    private final TemplateService templateService;
    private final DocumentImportService documentImportService;
    private final ComplianceEngine complianceEngine;
    private final DocxRenderService docxRenderService;
    private final FileStorageService fileStorageService;
    private final JsonCodec jsonCodec;

    public DocumentService(
            GovDocumentRepository govDocumentRepository,
            GovDocumentVersionRepository govDocumentVersionRepository,
            ValidationReportRepository validationReportRepository,
            ExportJobRepository exportJobRepository,
            TemplateService templateService,
            DocumentImportService documentImportService,
            ComplianceEngine complianceEngine,
            DocxRenderService docxRenderService,
            FileStorageService fileStorageService,
            JsonCodec jsonCodec
    ) {
        this.govDocumentRepository = govDocumentRepository;
        this.govDocumentVersionRepository = govDocumentVersionRepository;
        this.validationReportRepository = validationReportRepository;
        this.exportJobRepository = exportJobRepository;
        this.templateService = templateService;
        this.documentImportService = documentImportService;
        this.complianceEngine = complianceEngine;
        this.docxRenderService = docxRenderService;
        this.fileStorageService = fileStorageService;
        this.jsonCodec = jsonCodec;
    }

    public List<DocumentView> listDocuments() {
        return govDocumentRepository.findAll().stream()
                .sorted((left, right) -> right.getUpdatedAt().compareTo(left.getUpdatedAt()))
                .map(this::toView)
                .toList();
    }

    public DocumentView getDocument(String id) {
        return toView(findDocument(id));
    }

    @Transactional
    public DocumentView createDocument(DocumentUpsertRequest request) {
        GovDocumentEntity entity = new GovDocumentEntity();
        entity.setId(UUID.randomUUID().toString());
        entity.setTitle(request.title());
        entity.setDocType(request.metadata().getDocType().name());
        entity.setTemplateVersionId(request.templateVersionId());
        entity.setStatus(DocumentStatus.DRAFT.name());
        entity.setMetadataJson(jsonCodec.write(request.metadata()));
        entity.setBodyAstJson(jsonCodec.write(request.bodyBlocks()));
        entity.setLocalStyleOverridesJson(jsonCodec.write(request.localStyleOverrides()));
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        entity = govDocumentRepository.save(entity);
        createVersion(entity, 1);
        return toView(entity);
    }

    @Transactional
    public DocumentView updateDocument(String id, DocumentUpsertRequest request) {
        GovDocumentEntity entity = findDocument(id);
        entity.setTitle(request.title());
        entity.setDocType(request.metadata().getDocType().name());
        entity.setTemplateVersionId(request.templateVersionId());
        entity.setMetadataJson(jsonCodec.write(request.metadata()));
        entity.setBodyAstJson(jsonCodec.write(request.bodyBlocks()));
        entity.setLocalStyleOverridesJson(jsonCodec.write(request.localStyleOverrides()));
        entity.setUpdatedAt(Instant.now());
        entity = govDocumentRepository.save(entity);
        int nextVersion = govDocumentVersionRepository.findFirstByDocumentIdOrderByVersionNoDesc(entity.getId())
                .map(current -> current.getVersionNo() + 1)
                .orElse(1);
        createVersion(entity, nextVersion);
        return toView(entity);
    }

    public DocumentImportService.ImportResult importDocument(String format, String rawContent, MultipartFile file) {
        return documentImportService.importContent(format, rawContent, file);
    }

    @Transactional
    public ValidationResult validateDocument(String documentId) {
        GovDocumentEntity entity = findDocument(documentId);
        DocumentMetadata metadata = jsonCodec.read(entity.getMetadataJson(), DocumentMetadata.class);
        List<DocumentBlock> blocks = jsonCodec.readList(entity.getBodyAstJson(), DocumentBlock.class);
        TemplateRuleProfile ruleProfile = templateService.mergeRuleProfile(
                entity.getTemplateVersionId(),
                jsonCodec.read(entity.getLocalStyleOverridesJson(), TemplateRuleProfile.class)
        );
        ValidationReport report = complianceEngine.validate(metadata, blocks, ruleProfile);

        GovDocumentVersionEntity latestVersion = govDocumentVersionRepository.findFirstByDocumentIdOrderByVersionNoDesc(entity.getId())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "文稿版本不存在"));
        ValidationReportEntity reportEntity = new ValidationReportEntity();
        reportEntity.setId(UUID.randomUUID().toString());
        reportEntity.setDocumentId(entity.getId());
        reportEntity.setDocumentVersionId(latestVersion.getId());
        reportEntity.setPass(report.isPass());
        reportEntity.setReportJson(jsonCodec.write(report));
        reportEntity.setCreatedAt(Instant.now());
        validationReportRepository.save(reportEntity);

        entity.setStatus(report.isPass() ? DocumentStatus.READY.name() : DocumentStatus.DRAFT.name());
        entity.setUpdatedAt(Instant.now());
        govDocumentRepository.save(entity);

        return new ValidationResult(entity.getId(), report.isPass(), report.getIssues());
    }

    @Transactional
    public ExportJobView exportDocx(String documentId) {
        GovDocumentEntity entity = findDocument(documentId);
        ValidationResult validationResult = validateDocument(documentId);
        if (!validationResult.pass()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "当前文稿存在阻断级校验问题，无法导出");
        }

        ExportJobEntity job = new ExportJobEntity();
        job.setId(UUID.randomUUID().toString());
        job.setDocumentId(entity.getId());
        job.setTemplateVersionId(entity.getTemplateVersionId());
        job.setStatus(ExportJobStatus.PENDING.name());
        job.setSnapshotJson(entity.getBodyAstJson());
        job.setCreatedAt(Instant.now());
        job.setUpdatedAt(Instant.now());
        job = exportJobRepository.save(job);

        try {
            DocumentMetadata metadata = jsonCodec.read(entity.getMetadataJson(), DocumentMetadata.class);
            List<DocumentBlock> blocks = jsonCodec.readList(entity.getBodyAstJson(), DocumentBlock.class);
            TemplateRuleProfile mergedRuleProfile = templateService.mergeRuleProfile(
                    entity.getTemplateVersionId(),
                    jsonCodec.read(entity.getLocalStyleOverridesJson(), TemplateRuleProfile.class)
            );
            byte[] content = docxRenderService.renderDocument(metadata, blocks, mergedRuleProfile);
            job.setOutputPath(fileStorageService.writeExportFile(job.getId(), content));
            job.setStatus(ExportJobStatus.SUCCESS.name());
            entity.setStatus(DocumentStatus.EXPORTED.name());
            entity.setUpdatedAt(Instant.now());
            govDocumentRepository.save(entity);
        } catch (IOException exception) {
            job.setStatus(ExportJobStatus.FAILED.name());
            job.setErrorMessage(exception.getMessage());
        }

        job.setUpdatedAt(Instant.now());
        return toExportJobView(exportJobRepository.save(job));
    }

    @Transactional
    public TemplateService.TemplateVersionDetail saveAsTemplate(String documentId, SaveAsTemplateRequest request) {
        GovDocumentEntity entity = findDocument(documentId);
        TemplateRuleProfile mergedRuleProfile = templateService.mergeRuleProfile(
                entity.getTemplateVersionId(),
                request.styleOverrides() != null ? request.styleOverrides() : jsonCodec.read(entity.getLocalStyleOverridesJson(), TemplateRuleProfile.class)
        );
        return templateService.createDraftFromRule(entity.getTemplateVersionId(), request.name(), request.description(), mergedRuleProfile);
    }

    public List<ExportJobView> listExportJobs() {
        return exportJobRepository.findAllByOrderByCreatedAtDesc().stream().map(this::toExportJobView).toList();
    }

    public byte[] loadExportFile(String exportJobId) {
        ExportJobEntity entity = exportJobRepository.findById(exportJobId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "导出任务不存在"));
        if (entity.getOutputPath() == null) {
            throw new ApiException(HttpStatus.NOT_FOUND, "导出文件不存在");
        }
        try {
            return fileStorageService.readFile(entity.getOutputPath());
        } catch (IOException exception) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "读取导出文件失败");
        }
    }

    private void createVersion(GovDocumentEntity entity, int versionNo) {
        GovDocumentVersionEntity version = new GovDocumentVersionEntity();
        version.setId(UUID.randomUUID().toString());
        version.setDocumentId(entity.getId());
        version.setVersionNo(versionNo);
        version.setMetadataJson(entity.getMetadataJson());
        version.setBodyAstJson(entity.getBodyAstJson());
        version.setLocalStyleOverridesJson(entity.getLocalStyleOverridesJson());
        version.setCreatedAt(Instant.now());
        govDocumentVersionRepository.save(version);
    }

    private GovDocumentEntity findDocument(String id) {
        return govDocumentRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "文稿不存在"));
    }

    private DocumentView toView(GovDocumentEntity entity) {
        return new DocumentView(
                entity.getId(),
                entity.getTitle(),
                DocumentType.valueOf(entity.getDocType()),
                entity.getTemplateVersionId(),
                entity.getStatus(),
                jsonCodec.read(entity.getMetadataJson(), DocumentMetadata.class),
                jsonCodec.readList(entity.getBodyAstJson(), DocumentBlock.class),
                jsonCodec.read(entity.getLocalStyleOverridesJson(), TemplateRuleProfile.class),
                entity.getUpdatedAt()
        );
    }

    private ExportJobView toExportJobView(ExportJobEntity entity) {
        return new ExportJobView(entity.getId(), entity.getDocumentId(), entity.getTemplateVersionId(), entity.getStatus(), entity.getOutputPath(), entity.getErrorMessage(), entity.getUpdatedAt());
    }

    public record DocumentUpsertRequest(
            String title,
            String templateVersionId,
            DocumentMetadata metadata,
            List<DocumentBlock> bodyBlocks,
            TemplateRuleProfile localStyleOverrides
    ) {
        public DocumentUpsertRequest {
            if (bodyBlocks == null) {
                bodyBlocks = Collections.emptyList();
            }
        }
    }

    public record DocumentView(
            String id,
            String title,
            DocumentType docType,
            String templateVersionId,
            String status,
            DocumentMetadata metadata,
            List<DocumentBlock> bodyBlocks,
            TemplateRuleProfile localStyleOverrides,
            Instant updatedAt
    ) {
    }

    public record ValidationResult(String documentId, boolean pass, List<?> issues) {
    }

    public record ExportJobView(
            String id,
            String documentId,
            String templateVersionId,
            String status,
            String outputPath,
            String errorMessage,
            Instant updatedAt
    ) {
    }

    public record SaveAsTemplateRequest(String name, String description, TemplateRuleProfile styleOverrides) {
    }
}

