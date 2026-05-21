package com.docforge.govdoc;

import com.docforge.govdoc.document.DocumentBlock;
import com.docforge.govdoc.document.DocumentMetadata;
import com.docforge.govdoc.document.DocumentType;
import com.docforge.govdoc.template.TemplateRuleProfile;
import com.docforge.govdoc.validation.ComplianceEngine;
import com.docforge.govdoc.validation.ValidationSeverity;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ComplianceEngineTest {
    private final ComplianceEngine complianceEngine = new ComplianceEngine();

    @Test
    void shouldPassForValidRequestDocument() {
        TemplateRuleProfile profile = new TemplateRuleProfile();
        profile.getCompliance().setAllowedDocTypes(List.of(DocumentType.REQUEST));

        DocumentMetadata metadata = new DocumentMetadata();
        metadata.setDocType(DocumentType.REQUEST);
        metadata.setIssuingAuthority("某某局");
        metadata.setDocumentNumber("某政发〔2026〕1号");
        metadata.setSigner("李四");
        metadata.setMainRecipients("市政府：");
        metadata.setTitle("关于事项申请的请示");
        metadata.setSignatory("某某局");
        metadata.setIssuedAt(LocalDate.of(2026, 5, 20));
        metadata.setPrintAuthority("某某局办公室");
        metadata.setPrintIssuedAt(LocalDate.of(2026, 5, 20));

        var report = complianceEngine.validate(metadata, List.of(DocumentBlock.paragraph("正文内容")), profile);
        assertThat(report.isPass()).isTrue();
    }

    @Test
    void shouldBlockWhenRequiredFieldsMissing() {
        TemplateRuleProfile profile = new TemplateRuleProfile();
        profile.getCompliance().setAllowedDocTypes(List.of(DocumentType.NOTICE));

        DocumentMetadata metadata = new DocumentMetadata();
        metadata.setDocType(DocumentType.NOTICE);
        metadata.setTitle("示例标题");

        var report = complianceEngine.validate(metadata, List.of(), profile);
        assertThat(report.isPass()).isFalse();
        assertThat(report.getIssues())
                .extracting(issue -> issue.severity())
                .contains(ValidationSeverity.BLOCKING);
    }

    @Test
    void shouldBlockReportWithRequestPhrases() {
        TemplateRuleProfile profile = new TemplateRuleProfile();
        profile.getCompliance().setAllowedDocTypes(List.of(DocumentType.REPORT));

        DocumentMetadata metadata = new DocumentMetadata();
        metadata.setDocType(DocumentType.REPORT);
        metadata.setIssuingAuthority("某某局");
        metadata.setDocumentNumber("某政发〔2026〕2号");
        metadata.setMainRecipients("市政府：");
        metadata.setTitle("关于专项工作的报告");
        metadata.setSignatory("某某局");
        metadata.setIssuedAt(LocalDate.of(2026, 5, 20));
        metadata.setPrintAuthority("某某局办公室");
        metadata.setPrintIssuedAt(LocalDate.of(2026, 5, 20));

        var report = complianceEngine.validate(metadata, List.of(DocumentBlock.paragraph("以上事项妥否，请批复。")), profile);
        assertThat(report.isPass()).isFalse();
        assertThat(report.getIssues()).anyMatch(issue -> issue.message().contains("不得夹带请示事项"));
    }
}
