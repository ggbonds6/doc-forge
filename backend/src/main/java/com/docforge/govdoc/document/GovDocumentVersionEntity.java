package com.docforge.govdoc.document;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "document_version")
public class GovDocumentVersionEntity {
    @Id
    private String id;
    @Column(nullable = false)
    private String documentId;
    @Column(nullable = false)
    private int versionNo;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String metadataJson;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String bodyAstJson;
    @Column(columnDefinition = "TEXT")
    private String localStyleOverridesJson;
    @Column(nullable = false)
    private Instant createdAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public int getVersionNo() {
        return versionNo;
    }

    public void setVersionNo(int versionNo) {
        this.versionNo = versionNo;
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
}

