package com.docforge.govdoc.validation;

import com.docforge.govdoc.document.DocumentBlock;
import com.docforge.govdoc.document.DocumentMetadata;
import com.docforge.govdoc.document.DocumentType;
import com.docforge.govdoc.template.ComplianceProfile;
import com.docforge.govdoc.template.TemplateRuleProfile;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

@Component
public class ComplianceEngine {
    private static final Pattern DOCUMENT_NUMBER_PATTERN = Pattern.compile("^[^\\s]+〔\\d{4}〕\\d+号$");
    private static final Pattern STANDARD_DOCUMENT_NUMBER_PATTERN = Pattern.compile("^[\\u4e00-\\u9fa5A-Za-z]{1,12}〔\\d{4}〕\\d{1,5}号$");
    private static final Pattern HEADING_LEVEL_1_PATTERN = Pattern.compile("^[一二三四五六七八九十]+、.+");
    private static final Pattern HEADING_LEVEL_2_PATTERN = Pattern.compile("^（[一二三四五六七八九十]+）.+");
    private static final Pattern HEADING_LEVEL_3_PATTERN = Pattern.compile("^\\d+[.．].+");
    private static final List<String> REPORT_REQUEST_PHRASES = List.of("请批示", "请审批", "请予批准", "妥否，请批复", "当否，请批示");

    public ValidationReport validate(DocumentMetadata metadata, List<DocumentBlock> blocks, TemplateRuleProfile ruleProfile) {
        List<ValidationIssue> issues = new ArrayList<>();
        ComplianceProfile profile = ruleProfile.getCompliance();

        if (metadata == null) {
            issues.add(new ValidationIssue(ValidationSeverity.BLOCKING, "metadata.missing", "metadata", "缺少公文元数据"));
        } else {
            validateMetadata(metadata, profile, issues);
        }

        validateBlocks(metadata == null ? null : metadata.getDocType(), blocks, issues);

        ValidationReport report = new ValidationReport();
        report.setIssues(issues);
        report.setPass(issues.stream().noneMatch(issue -> issue.severity() == ValidationSeverity.BLOCKING));
        return report;
    }

    private void validateMetadata(DocumentMetadata metadata, ComplianceProfile profile, List<ValidationIssue> issues) {
        if (metadata.getDocType() == null) {
            issues.add(blocking("metadata.docType", "文种不能为空"));
            return;
        }

        if (!profile.getAllowedDocTypes().isEmpty() && !profile.getAllowedDocTypes().contains(metadata.getDocType())) {
            issues.add(blocking("metadata.docType", "当前模板不支持该文种"));
        }
        requireText(metadata.getIssuingAuthority(), "metadata.issuingAuthority", "发文机关标志不能为空", issues);
        requireText(metadata.getMainRecipients(), "metadata.mainRecipients", "主送机关不能为空", issues);
        requireText(metadata.getTitle(), "metadata.title", "标题不能为空", issues);
        requireDate(metadata.getIssuedAt(), "metadata.issuedAt", "成文日期不能为空", issues);
        requireText(metadata.getSignatory(), "metadata.signatory", "署名机关不能为空", issues);

        if (Boolean.TRUE.equals(profile.getRequireDocumentNumber())) {
            requireText(metadata.getDocumentNumber(), "metadata.documentNumber", "发文字号不能为空", issues);
            if (hasText(metadata.getDocumentNumber()) && !DOCUMENT_NUMBER_PATTERN.matcher(metadata.getDocumentNumber()).matches()) {
                issues.add(blocking("metadata.documentNumber", "发文字号格式应为“机关代字〔年份〕序号号”"));
            }
            if (hasText(metadata.getDocumentNumber()) && !STANDARD_DOCUMENT_NUMBER_PATTERN.matcher(metadata.getDocumentNumber()).matches()) {
                issues.add(warning("metadata.documentNumber", "发文字号建议使用 1-12 位机关代字、4 位年份和 1-5 位序号"));
            }
            if (hasText(metadata.getDocumentNumber()) && (metadata.getDocumentNumber().contains("[") || metadata.getDocumentNumber().contains("]"))) {
                issues.add(blocking("metadata.documentNumber", "发文字号年份必须使用六角括号“〔〕”，不能使用英文方括号"));
            }
        }

        if (Boolean.TRUE.equals(profile.getRequireSignerForRequest()) && metadata.getDocType() == DocumentType.REQUEST) {
            requireText(metadata.getSigner(), "metadata.signer", "请示类公文必须填写签发人", issues);
        }

        if (Boolean.TRUE.equals(profile.getEnforceTitleSuffix()) && hasText(metadata.getTitle())) {
            String suffix = metadata.getDocType().displayName();
            if (!metadata.getTitle().endsWith(suffix)) {
                issues.add(blocking("metadata.title", "标题应以“" + suffix + "”结尾"));
            }
        }

        if (hasText(metadata.getTitle()) && metadata.getTitle().length() > 60) {
            issues.add(warning("metadata.title", "标题过长，建议核对是否需要拆分事由或调整文种"));
        }

        if (hasText(metadata.getMainRecipients()) && !metadata.getMainRecipients().trim().endsWith("：")) {
            issues.add(blocking("metadata.mainRecipients", "主送机关末尾应使用中文冒号“：”"));
        }

        if (metadata.getDocType() == DocumentType.REQUEST && hasText(metadata.getMainRecipients()) && looksLikeMultipleRecipients(metadata.getMainRecipients())) {
            issues.add(warning("metadata.mainRecipients", "请示一般应主送一个上级机关，请确认主送机关是否唯一"));
        }

        if (Boolean.TRUE.equals(profile.getRequirePrintFooter())) {
            requireText(metadata.getPrintAuthority(), "metadata.printAuthority", "版记中的印发机关不能为空", issues);
            requireDate(metadata.getPrintIssuedAt(), "metadata.printIssuedAt", "版记中的印发日期不能为空", issues);
        }

        if (metadata.getIssuedAt() != null && metadata.getIssuedAt().isAfter(LocalDate.now(ZoneId.of("Asia/Shanghai")))) {
            issues.add(blocking("metadata.issuedAt", "成文日期不能晚于当前日期"));
        }

        if (metadata.getIssuedAt() != null && metadata.getPrintIssuedAt() != null && metadata.getPrintIssuedAt().isBefore(metadata.getIssuedAt())) {
            issues.add(warning("metadata.printIssuedAt", "印发日期早于成文日期，请确认版记日期"));
        }

        if (!hasText(metadata.getSecrecyLevel())) {
            issues.add(warning("metadata.secrecyLevel", "未填写密级，如无密级可忽略"));
        }
        if (!hasText(metadata.getUrgencyLevel())) {
            issues.add(warning("metadata.urgencyLevel", "未填写紧急程度，如无紧急程度可忽略"));
        }
        if (hasText(metadata.getAttachmentNote()) && metadata.getAttachmentNote().contains("附件")) {
            if (!metadata.getAttachmentNote().trim().startsWith("附件：")) {
                issues.add(blocking("metadata.attachmentNote", "附件说明应以“附件：”开头"));
            } else {
                issues.add(warning("metadata.attachmentNote", "存在附件说明时，请确认正文中已列出对应附件清单"));
            }
        }
    }

    private void validateBlocks(DocumentType docType, List<DocumentBlock> blocks, List<ValidationIssue> issues) {
        if (blocks == null || blocks.isEmpty()) {
            issues.add(blocking("body", "正文不能为空"));
            return;
        }

        int previousHeadingLevel = 0;
        boolean hasTextBlock = false;
        String allText = "";
        for (int index = 0; index < blocks.size(); index++) {
            DocumentBlock block = blocks.get(index);
            if (block == null || !hasText(block.getType())) {
                issues.add(blocking("body[" + index + "]", "存在无法识别的正文块"));
                continue;
            }

            if (Objects.equals(block.getType(), "heading")) {
                int currentLevel = block.getLevel() == null ? 1 : block.getLevel();
                if (currentLevel < 1 || currentLevel > 3) {
                    issues.add(blocking("body[" + index + "]", "公文正文标题层级暂仅支持 1-3 级"));
                }
                if (previousHeadingLevel > 0 && currentLevel - previousHeadingLevel > 1) {
                    issues.add(warning("body[" + index + "]", "标题层级跳跃过大，建议按顺序使用标题级别"));
                }
                validateHeadingNumber(index, currentLevel, block.getText(), issues);
                previousHeadingLevel = currentLevel;
            }

            if (Objects.equals(block.getType(), "paragraph") || Objects.equals(block.getType(), "heading")) {
                if (hasText(block.getText())) {
                    hasTextBlock = true;
                    allText = allText + "\n" + block.getText();
                } else {
                    issues.add(blocking("body[" + index + "]", "段落或标题内容不能为空"));
                }
                if (block.getText() != null && !block.getText().isBlank() && !block.getText().equals(block.getText().stripLeading())) {
                    issues.add(warning("body[" + index + "]", "正文块开头存在手工空格，建议使用模板首行缩进控制排版"));
                }
            } else if (Objects.equals(block.getType(), "list")) {
                if (block.getItems() == null || block.getItems().isEmpty()) {
                    issues.add(blocking("body[" + index + "]", "列表内容不能为空"));
                } else {
                    hasTextBlock = true;
                    allText = allText + "\n" + String.join("\n", block.getItems());
                }
            } else if (Objects.equals(block.getType(), "table")) {
                validateTable(index, block, issues);
                hasTextBlock = block.getRows() != null && !block.getRows().isEmpty();
            } else if (!Objects.equals(block.getType(), "image")) {
                issues.add(blocking("body[" + index + "]", "不支持的正文块类型：" + block.getType()));
            }
        }

        if (!hasTextBlock) {
            issues.add(blocking("body", "正文没有可导出的有效内容"));
        }

        if (docType == DocumentType.REPORT && containsAny(allText, REPORT_REQUEST_PHRASES)) {
            issues.add(blocking("body", "报告中不得夹带请示事项；如需请求批准，请改用“请示”文种"));
        }
    }

    private void validateHeadingNumber(int index, int level, String text, List<ValidationIssue> issues) {
        if (!hasText(text)) {
            return;
        }
        if (level == 1 && !HEADING_LEVEL_1_PATTERN.matcher(text).matches()) {
            issues.add(warning("body[" + index + "]", "一级标题建议使用“一、”格式"));
        }
        if (level == 2 && !HEADING_LEVEL_2_PATTERN.matcher(text).matches()) {
            issues.add(warning("body[" + index + "]", "二级标题建议使用“（一）”格式"));
        }
        if (level == 3 && !HEADING_LEVEL_3_PATTERN.matcher(text).matches()) {
            issues.add(warning("body[" + index + "]", "三级标题建议使用“1.”格式"));
        }
    }

    private void validateTable(int index, DocumentBlock block, List<ValidationIssue> issues) {
        if (block.getRows() == null || block.getRows().isEmpty()) {
            issues.add(blocking("body[" + index + "]", "表格内容不能为空"));
            return;
        }
        int expectedColumns = block.getRows().get(0).size();
        if (expectedColumns == 0) {
            issues.add(blocking("body[" + index + "]", "表格至少需要一列"));
            return;
        }
        for (int rowIndex = 0; rowIndex < block.getRows().size(); rowIndex++) {
            List<String> row = block.getRows().get(rowIndex);
            if (row.size() != expectedColumns) {
                issues.add(blocking("body[" + index + "].rows[" + rowIndex + "]", "表格每行列数必须一致"));
            }
            if (row.stream().allMatch(cell -> cell == null || cell.isBlank())) {
                issues.add(warning("body[" + index + "].rows[" + rowIndex + "]", "表格存在空行，请确认是否需要保留"));
            }
        }
    }

    private boolean looksLikeMultipleRecipients(String recipients) {
        return Arrays.stream(new String[]{"、", "，", ",", "；", ";", "及", "和"})
                .anyMatch(recipients::contains);
    }

    private boolean containsAny(String text, List<String> phrases) {
        return phrases.stream().anyMatch(text::contains);
    }

    private void requireText(String value, String field, String message, List<ValidationIssue> issues) {
        if (!hasText(value)) {
            issues.add(blocking(field, message));
        }
    }

    private void requireDate(Object value, String field, String message, List<ValidationIssue> issues) {
        if (value == null) {
            issues.add(blocking(field, message));
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private ValidationIssue blocking(String field, String message) {
        return new ValidationIssue(ValidationSeverity.BLOCKING, field, field, message);
    }

    private ValidationIssue warning(String field, String message) {
        return new ValidationIssue(ValidationSeverity.WARNING, field, field, message);
    }
}
