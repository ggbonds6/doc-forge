package com.docforge.govdoc;

import com.docforge.govdoc.document.DocumentBlock;
import com.docforge.govdoc.document.DocumentMetadata;
import com.docforge.govdoc.document.DocumentType;
import com.docforge.govdoc.render.DocxRenderService;
import com.docforge.govdoc.template.TemplateRuleProfile;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DocxRenderServiceTest {
    private final DocxRenderService renderService = new DocxRenderService();

    @Test
    void shouldRenderDocxWithTitleAndParagraph() throws Exception {
        TemplateRuleProfile profile = new TemplateRuleProfile();
        profile.getParagraphStyle().setFontFamily("仿宋");
        profile.getParagraphStyle().setFontSize(16);
        profile.getTitleStyle().setFontFamily("方正小标宋简体");
        profile.getTitleStyle().setFontSize(22);

        DocumentMetadata metadata = new DocumentMetadata();
        metadata.setDocType(DocumentType.NOTICE);
        metadata.setIssuingAuthority("某某机关");
        metadata.setDocumentNumber("某政发〔2026〕1号");
        metadata.setMainRecipients("各单位：");
        metadata.setTitle("关于事项安排的通知");
        metadata.setSignatory("某某机关");
        metadata.setIssuedAt(LocalDate.of(2026, 5, 20));
        metadata.setPrintAuthority("某某机关办公室");
        metadata.setPrintIssuedAt(LocalDate.of(2026, 5, 20));

        byte[] bytes = renderService.renderDocument(metadata, List.of(DocumentBlock.paragraph("正文段落")), profile);
        assertThat(bytes).isNotEmpty();

        try (XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(bytes))) {
            String allText = document.getParagraphs().stream().map(p -> p.getText()).reduce("", String::concat);
            assertThat(allText).contains("关于事项安排的通知");
            assertThat(allText).contains("正文段落");
        }
    }
}

