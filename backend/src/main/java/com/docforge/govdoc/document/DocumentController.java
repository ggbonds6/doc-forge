package com.docforge.govdoc.document;

import com.docforge.govdoc.template.TemplateService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RestController
@RequestMapping("/api")
public class DocumentController {
    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @GetMapping("/documents")
    public List<DocumentService.DocumentView> listDocuments() {
        return documentService.listDocuments();
    }

    @GetMapping("/documents/{id}")
    public DocumentService.DocumentView getDocument(@PathVariable String id) {
        return documentService.getDocument(id);
    }

    @PostMapping("/documents")
    public DocumentService.DocumentView createDocument(@Valid @RequestBody DocumentService.DocumentUpsertRequest request) {
        return documentService.createDocument(request);
    }

    @PutMapping("/documents/{id}")
    public DocumentService.DocumentView updateDocument(@PathVariable String id, @Valid @RequestBody DocumentService.DocumentUpsertRequest request) {
        return documentService.updateDocument(id, request);
    }

    @PostMapping("/documents/import")
    public DocumentImportService.ImportResult importDocument(@ModelAttribute ImportBody body) {
        return documentService.importDocument(body.format(), body.rawContent(), body.file());
    }

    @PostMapping("/documents/{id}/validate")
    public DocumentService.ValidationResult validateDocument(@PathVariable String id) {
        return documentService.validateDocument(id);
    }

    @PostMapping("/documents/{id}/export-docx")
    public DocumentService.ExportJobView exportDocx(@PathVariable String id) {
        return documentService.exportDocx(id);
    }

    @PostMapping("/documents/{id}/save-as-template")
    public TemplateService.TemplateVersionDetail saveAsTemplate(@PathVariable String id, @Valid @RequestBody SaveAsTemplateBody body) {
        return documentService.saveAsTemplate(id, new DocumentService.SaveAsTemplateRequest(body.name(), body.description(), body.styleOverrides()));
    }

    @GetMapping("/export-jobs")
    public List<DocumentService.ExportJobView> listExportJobs() {
        return documentService.listExportJobs();
    }

    @GetMapping("/export-jobs/{id}/download")
    public ResponseEntity<byte[]> downloadExport(@PathVariable String id) {
        byte[] content = documentService.loadExportFile(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(ContentDisposition.attachment().filename("govdoc-" + id + ".docx", StandardCharsets.UTF_8).build());
        return ResponseEntity.ok().headers(headers).body(content);
    }

    public record ImportBody(String format, String rawContent, MultipartFile file) {
    }

    public record SaveAsTemplateBody(
            @NotBlank(message = "模板名称不能为空") String name,
            String description,
            com.docforge.govdoc.template.TemplateRuleProfile styleOverrides
    ) {
    }
}

