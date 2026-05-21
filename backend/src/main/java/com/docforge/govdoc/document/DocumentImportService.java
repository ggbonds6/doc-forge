package com.docforge.govdoc.document;

import com.docforge.govdoc.common.ApiException;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class DocumentImportService {
    private final Parser markdownParser;
    private final HtmlRenderer htmlRenderer;

    public DocumentImportService() {
        List<Extension> extensions = List.of(TablesExtension.create());
        this.markdownParser = Parser.builder().extensions(extensions).build();
        this.htmlRenderer = HtmlRenderer.builder().extensions(extensions).build();
    }

    public ImportResult importContent(String format, String rawContent, MultipartFile file) {
        try {
            return switch (normalizeFormat(format, file)) {
                case "markdown" -> new ImportResult(parseMarkdown(readRawContent(rawContent, file)));
                case "html" -> new ImportResult(parseHtml(readRawContent(rawContent, file)));
                case "docx" -> new ImportResult(parseDocx(file));
                default -> new ImportResult(parsePlainText(readRawContent(rawContent, file)));
            };
        } catch (IOException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "导入文件失败：" + exception.getMessage());
        }
    }

    private String normalizeFormat(String format, MultipartFile file) {
        if (format != null && !format.isBlank()) {
            return format.toLowerCase();
        }
        if (file != null && file.getOriginalFilename() != null && file.getOriginalFilename().endsWith(".docx")) {
            return "docx";
        }
        return "plain";
    }

    private String readRawContent(String rawContent, MultipartFile file) throws IOException {
        if (rawContent != null && !rawContent.isBlank()) {
            return rawContent;
        }
        if (file != null && !file.isEmpty()) {
            return new String(file.getBytes(), StandardCharsets.UTF_8);
        }
        return "";
    }

    private List<DocumentBlock> parsePlainText(String text) {
        List<DocumentBlock> blocks = new ArrayList<>();
        for (String line : text.split("\\R\\R+")) {
            String content = line.trim();
            if (content.isBlank()) {
                continue;
            }
            blocks.add(DocumentBlock.paragraph(content.replace('\n', ' ')));
        }
        return blocks;
    }

    private List<DocumentBlock> parseMarkdown(String markdown) {
        Node node = markdownParser.parse(markdown);
        return parseHtml(htmlRenderer.render(node));
    }

    private List<DocumentBlock> parseHtml(String html) {
        Document document = Jsoup.parseBodyFragment(html);
        List<DocumentBlock> blocks = new ArrayList<>();
        for (Element element : document.body().children()) {
            switch (element.tagName()) {
                case "h1" -> blocks.add(DocumentBlock.heading(1, element.text()));
                case "h2" -> blocks.add(DocumentBlock.heading(2, element.text()));
                case "h3" -> blocks.add(DocumentBlock.heading(3, element.text()));
                case "ul", "ol" -> {
                    DocumentBlock block = new DocumentBlock();
                    block.setType("list");
                    block.setOrdered("ol".equals(element.tagName()));
                    block.setItems(element.select("> li").eachText());
                    blocks.add(block);
                }
                case "table" -> {
                    DocumentBlock block = new DocumentBlock();
                    block.setType("table");
                    List<List<String>> rows = new ArrayList<>();
                    for (Element row : element.select("tr")) {
                        rows.add(row.select("th,td").eachText());
                    }
                    block.setRows(rows);
                    blocks.add(block);
                }
                case "img" -> {
                    DocumentBlock block = new DocumentBlock();
                    block.setType("image");
                    block.setSrc(element.attr("src"));
                    block.setCaption(element.attr("alt"));
                    blocks.add(block);
                }
                default -> blocks.add(DocumentBlock.paragraph(element.text()));
            }
        }
        return blocks;
    }

    private List<DocumentBlock> parseDocx(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "DOCX 文件不能为空");
        }
        try (XWPFDocument document = new XWPFDocument(new ByteArrayInputStream(file.getBytes()))) {
            List<DocumentBlock> blocks = new ArrayList<>();
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                String text = paragraph.getText();
                if (text != null && !text.isBlank()) {
                    blocks.add(DocumentBlock.paragraph(text));
                }
            }
            for (XWPFTable table : document.getTables()) {
                DocumentBlock block = new DocumentBlock();
                block.setType("table");
                List<List<String>> rows = new ArrayList<>();
                table.getRows().forEach(row -> rows.add(row.getTableCells().stream().map(org.apache.poi.xwpf.usermodel.XWPFTableCell::getText).toList()));
                block.setRows(rows);
                blocks.add(block);
            }
            return blocks;
        }
    }

    public record ImportResult(List<DocumentBlock> blocks) {
    }
}

