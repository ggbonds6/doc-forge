package com.docforge.govdoc.document;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "document_record")
public class GovDocumentEntity {
    @Id
    private String id;
    @Column(nullable = false)
    private String title;
    @Column(nullable = false)
    private String docType;
    @Column(nullable = false)
    private String templateVersionId;
    @Column(nullable = false)
    private String status;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String metadataJson;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String bodyAstJson;
    @Column(columnDefinition = "TEXT")
    private String localStyleOverridesJson;
    @Column(nullable = false)
    private Instant createdAt;
    @Column(nullable = false)
    private Instant updatedAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDocType() {
        return docType;
    }

    public void setDocType(String docType) {
        this.docType = docType;
    }

    public String getTemplateVersionId() {
        return templateVersionId;
    }

    public void setTemplateVersionId(String templateVersionId) {
        this.templateVersionId = templateVersionId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMetadataJson() {
        return metadataJson;
    }

    public void setMetadataJson(String metadataJson) {
        this.metadataJson = metadataJson;
    }

    public String getBodyAstJson() {
        return bodyAstJson;
    }

    public void setBodyAstJson(String bodyAstJson) {
        this.bodyAstJson = bodyAstJson;
    }

    public String getLocalStyleOverridesJson() {
        return localStyleOverridesJson;
    }

    public void setLocalStyleOverridesJson(String localStyleOverridesJson) {
        this.localStyleOverridesJson = localStyleOverridesJson;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}

