package com.docforge.govdoc.render;

import com.docforge.govdoc.document.DocumentBlock;
import com.docforge.govdoc.document.DocumentMetadata;
import com.docforge.govdoc.document.DocumentType;
import com.docforge.govdoc.template.TemplateRuleProfile;
import com.docforge.govdoc.template.TemplateTableStyle;
import com.docforge.govdoc.template.TemplateTextStyle;
import org.apache.poi.xwpf.usermodel.BreakType;
import org.apache.poi.xwpf.usermodel.Borders;
import org.apache.poi.xwpf.usermodel.LineSpacingRule;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFFooter;
import org.apache.poi.xwpf.usermodel.XWPFHeader;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageMar;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSimpleField;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STPageOrientation;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STSectionMark;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class DocxRenderService {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy年M月d日");

    public byte[] renderDocument(DocumentMetadata metadata, List<DocumentBlock> blocks, TemplateRuleProfile ruleProfile) {
        try (XWPFDocument document = new XWPFDocument(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            configurePage(document, ruleProfile);
            createHeader(document, metadata, ruleProfile);
            createFooter(document, metadata, ruleProfile);
            renderMeta(document, metadata, ruleProfile);
            renderBody(document, blocks, ruleProfile);
            renderTail(document, metadata, ruleProfile);
            document.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("DOCX 导出失败", exception);
        }
    }

    public byte[] renderTemplatePreview(DocumentType documentType, TemplateRuleProfile ruleProfile) {
        DocumentMetadata metadata = new DocumentMetadata();
        metadata.setDocType(documentType);
        metadata.setIssuingAuthority("某某机关");
        metadata.setDocumentNumber("某政发〔2026〕1号");
        metadata.setMainRecipients("各有关单位：");
        metadata.setTitle("关于开展标准化示范工作的" + documentType.displayName());
        metadata.setSigner(documentType == DocumentType.REQUEST ? "张三" : null);
        metadata.setSignatory("某某机关");
        metadata.setIssuedAt(java.time.LocalDate.of(2026, 5, 20));
        metadata.setPrintAuthority("某某机关办公室");
        metadata.setPrintIssuedAt(java.time.LocalDate.of(2026, 5, 20));
        List<DocumentBlock> blocks = List.of(
                DocumentBlock.paragraph("这是模板预览示例，用于验证标题、正文、页边距、页码和版记等基础版式是否符合要求。"),
                DocumentBlock.heading(1, "一、工作目标"),
                DocumentBlock.paragraph("通过内置模板、严格校验和 DOCX 导出能力，形成稳定的正式公文生产链路。")
        );
        return renderDocument(metadata, blocks, ruleProfile);
    }

    private void configurePage(XWPFDocument document, TemplateRuleProfile ruleProfile) {
        CTSectPr sectPr = document.getDocument().getBody().isSetSectPr()
                ? document.getDocument().getBody().getSectPr()
                : document.getDocument().getBody().addNewSectPr();
        sectPr.addNewType().setVal(STSectionMark.NEXT_PAGE);
        sectPr.addNewPgSz().setW(java.math.BigInteger.valueOf(11906));
        sectPr.getPgSz().setH(java.math.BigInteger.valueOf(16838));
        sectPr.getPgSz().setOrient(STPageOrientation.PORTRAIT);
        CTPageMar pageMar = sectPr.isSetPgMar() ? sectPr.getPgMar() : sectPr.addNewPgMar();
        pageMar.setTop(java.math.BigInteger.valueOf(toTwips(ruleProfile.getPage().getMarginTop())));
        pageMar.setBottom(java.math.BigInteger.valueOf(toTwips(ruleProfile.getPage().getMarginBottom())));
        pageMar.setLeft(java.math.BigInteger.valueOf(toTwips(ruleProfile.getPage().getMarginLeft())));
        pageMar.setRight(java.math.BigInteger.valueOf(toTwips(ruleProfile.getPage().getMarginRight())));
    }

    private void createHeader(XWPFDocument document, DocumentMetadata metadata, TemplateRuleProfile ruleProfile) {
        XWPFHeader header = document.createHeader(org.apache.poi.wp.usermodel.HeaderFooterType.DEFAULT);
        XWPFParagraph paragraph = header.createParagraph();
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun run = paragraph.createRun();
        TemplateTextStyle style = ruleProfile.getTitleStyle();
        applyTextStyle(run, style);
        run.setColor("C00000");
        run.setText(metadata.getIssuingAuthority() == null ? "公文模板" : metadata.getIssuingAuthority());
    }

    private void createFooter(XWPFDocument document, DocumentMetadata metadata, TemplateRuleProfile ruleProfile) {
        if (!Boolean.TRUE.equals(ruleProfile.getHeaderFooter().getShowPageNumber())) {
            return;
        }
        XWPFFooter footer = document.createFooter(org.apache.poi.wp.usermodel.HeaderFooterType.DEFAULT);
        XWPFParagraph paragraph = footer.createParagraph();
        paragraph.setAlignment(ParagraphAlignment.CENTER);
        paragraph.createRun().setText("— ");
        CTSimpleField simpleField = paragraph.getCTP().addNewFldSimple();
        simpleField.setInstr(" PAGE ");
        simpleField.addNewR().addNewT().setStringValue("1");
        paragraph.createRun().setText(" —");
    }

    private void renderMeta(XWPFDocument document, DocumentMetadata metadata, TemplateRuleProfile ruleProfile) {
        XWPFParagraph numberLine = document.createParagraph();
        numberLine.setAlignment(ParagraphAlignment.BOTH);
        XWPFRun leftRun = numberLine.createRun();
        applyTextStyle(leftRun, ruleProfile.getParagraphStyle());
        leftRun.setText(defaultText(metadata.getDocumentNumber()));
        if (metadata.getSigner() != null && !metadata.getSigner().isBlank()) {
            XWPFRun space = numberLine.createRun();
            space.setText("\t");
            XWPFRun rightRun = numberLine.createRun();
            applyTextStyle(rightRun, ruleProfile.getParagraphStyle());
            rightRun.setText("签发人：" + metadata.getSigner());
        }
        numberLine.setBorderBottom(Borders.SINGLE);
        if (numberLine.getCTP().getPPr() != null && numberLine.getCTP().getPPr().getPBdr() != null) {
            numberLine.getCTP().getPPr().getPBdr().getBottom().setColor("C00000");
            numberLine.getCTP().getPPr().getPBdr().getBottom().setSz(BigInteger.valueOf(12));
        }

        XWPFParagraph title = document.createParagraph();
        title.setAlignment(ParagraphAlignment.CENTER);
        XWPFRun titleRun = title.createRun();
        applyTextStyle(titleRun, ruleProfile.getTitleStyle());
        titleRun.setText(defaultText(metadata.getTitle()));

        XWPFParagraph recipient = document.createParagraph();
        recipient.setAlignment(ParagraphAlignment.LEFT);
        XWPFRun recipientRun = recipient.createRun();
        applyTextStyle(recipientRun, ruleProfile.getParagraphStyle());
        recipientRun.setText(defaultText(metadata.getMainRecipients()));
    }

    private void renderBody(XWPFDocument document, List<DocumentBlock> blocks, TemplateRuleProfile ruleProfile) {
        for (DocumentBlock block : blocks) {
            if (block == null || block.getType() == null) {
                continue;
            }
            switch (block.getType()) {
                case "heading" -> renderHeading(document, block, ruleProfile);
                case "list" -> renderList(document, block, ruleProfile);
                case "table" -> renderTable(document, block, ruleProfile);
                case "image" -> renderImagePlaceholder(document, block, ruleProfile);
                default -> renderParagraph(document, block.getText(), ruleProfile.getParagraphStyle());
            }
        }
    }

    private void renderHeading(XWPFDocument document, DocumentBlock block, TemplateRuleProfile ruleProfile) {
        TemplateTextStyle style = switch (block.getLevel() == null ? 1 : block.getLevel()) {
            case 1 -> ruleProfile.getHeading1Style();
            case 2 -> ruleProfile.getHeading2Style();
            default -> ruleProfile.getHeading3Style();
        };
        renderParagraph(document, block.getText(), style);
    }

    private void renderList(XWPFDocument document, DocumentBlock block, TemplateRuleProfile ruleProfile) {
        if (block.getItems() == null) {
            return;
        }
        int counter = 1;
        for (String item : block.getItems()) {
            String prefix = Boolean.TRUE.equals(block.getOrdered()) ? counter++ + ". " : "• ";
            renderParagraph(document, prefix + item, ruleProfile.getParagraphStyle());
        }
    }

    private void renderTable(XWPFDocument document, DocumentBlock block, TemplateRuleProfile ruleProfile) {
        if (block.getRows() == null || block.getRows().isEmpty()) {
            return;
        }
        XWPFTable table = document.createTable(block.getRows().size(), block.getRows().get(0).size());
        TemplateTableStyle tableStyle = ruleProfile.getTableStyle();
        for (int i = 0; i < block.getRows().size(); i++) {
            XWPFTableRow row = table.getRow(i);
            List<String> rowData = block.getRows().get(i);
            for (int j = 0; j < rowData.size(); j++) {
                XWPFTableCell cell = row.getCell(j);
                cell.removeParagraph(0);
                XWPFParagraph paragraph = cell.addParagraph();
                XWPFRun run = paragraph.createRun();
                run.setFontFamily(tableStyle.getFontFamily());
                run.setFontSize(tableStyle.getFontSize());
                run.setText(rowData.get(j));
            }
        }
    }

    private void renderImagePlaceholder(XWPFDocument document, DocumentBlock block, TemplateRuleProfile ruleProfile) {
        renderParagraph(document, "[图片占位] " + defaultText(block.getCaption()), ruleProfile.getParagraphStyle());
    }

    private void renderParagraph(XWPFDocument document, String text, TemplateTextStyle style) {
        XWPFParagraph paragraph = document.createParagraph();
        paragraph.setAlignment(toAlignment(style.getAlign()));
        if (style.getSpacingBefore() != null) {
            paragraph.setSpacingBefore(style.getSpacingBefore());
        }
        if (style.getSpacingAfter() != null) {
            paragraph.setSpacingAfter(style.getSpacingAfter());
        }
        if (style.getLineHeight() != null) {
            paragraph.setSpacingBetween(style.getLineHeight(), LineSpacingRule.AUTO);
        }
        if (style.getFirstLineIndent() != null && style.getFirstLineIndent().endsWith("em")) {
            paragraph.setIndentationFirstLine(420 * Integer.parseInt(style.getFirstLineIndent().replace("em", "")));
        }
        XWPFRun run = paragraph.createRun();
        applyTextStyle(run, style);
        run.setText(defaultText(text));
    }

    private void renderTail(XWPFDocument document, DocumentMetadata metadata, TemplateRuleProfile ruleProfile) {
        if (metadata.getAttachmentNote() != null && !metadata.getAttachmentNote().isBlank()) {
            renderParagraph(document, metadata.getAttachmentNote(), ruleProfile.getParagraphStyle());
        }

        XWPFParagraph sign = document.createParagraph();
        sign.setAlignment(ParagraphAlignment.RIGHT);
        XWPFRun signRun = sign.createRun();
        applyTextStyle(signRun, ruleProfile.getParagraphStyle());
        signRun.setText(defaultText(metadata.getSignatory()));
        signRun.addBreak(BreakType.TEXT_WRAPPING);
        if (metadata.getIssuedAt() != null) {
            signRun.setText(DATE_FORMATTER.format(metadata.getIssuedAt()));
        }

        XWPFParagraph print = document.createParagraph();
        print.setAlignment(ParagraphAlignment.LEFT);
        XWPFRun printRun = print.createRun();
        applyTextStyle(printRun, ruleProfile.getParagraphStyle());
        printRun.setText(defaultText(metadata.getPrintAuthority()));
        if (metadata.getPrintIssuedAt() != null) {
            printRun.addBreak(BreakType.TEXT_WRAPPING);
            printRun.setText(DATE_FORMATTER.format(metadata.getPrintIssuedAt()));
        }
    }

    private void applyTextStyle(XWPFRun run, TemplateTextStyle style) {
        if (style.getFontFamily() != null) {
            run.setFontFamily(style.getFontFamily());
        }
        if (style.getFontSize() != null) {
            run.setFontSize(style.getFontSize());
        }
        if (style.getBold() != null) {
            run.setBold(style.getBold());
        }
        if (style.getColor() != null) {
            run.setColor(style.getColor());
        }
    }

    private ParagraphAlignment toAlignment(String align) {
        if (align == null) {
            return ParagraphAlignment.LEFT;
        }
        return switch (align.toLowerCase()) {
            case "center" -> ParagraphAlignment.CENTER;
            case "right" -> ParagraphAlignment.RIGHT;
            case "both", "justify" -> ParagraphAlignment.BOTH;
            default -> ParagraphAlignment.LEFT;
        };
    }

    private long toTwips(String sizeText) {
        if (sizeText == null || sizeText.isBlank()) {
            return 1440;
        }
        String value = sizeText.trim().toLowerCase();
        if (value.endsWith("cm")) {
            double cm = Double.parseDouble(value.replace("cm", ""));
            return Math.round(cm * 567);
        }
        if (value.endsWith("mm")) {
            double mm = Double.parseDouble(value.replace("mm", ""));
            return Math.round(mm * 56.7);
        }
        return Long.parseLong(value);
    }

    private String defaultText(String text) {
        return text == null ? "" : text;
    }
}
