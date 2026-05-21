package com.docforge.govdoc.template;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "template_version")
public class TemplateVersionEntity {
    @Id
    private String id;
    @Column(nullable = false)
    private String templatePackId;
    @Column(nullable = false)
    private String versionLabel;
    @Column(nullable = false)
    private String status;
    @Column(nullable = false)
    private String name;
    @Column(columnDefinition = "TEXT")
    private String description;
    @Column(nullable = false, columnDefinition = "TEXT")
    private String ruleProfileJson;
    @Column(columnDefinition = "TEXT")
    private String previewBaselineJson;
    @Column
    private String compiledTemplatePath;
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

    public String getTemplatePackId() {
        return templatePackId;
    }

    public void setTemplatePackId(String templatePackId) {
        this.templatePackId = templatePackId;
    }

    public String getVersionLabel() {
        return versionLabel;
    }

    public void setVersionLabel(String versionLabel) {
        this.versionLabel = versionLabel;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRuleProfileJson() {
        return ruleProfileJson;
    }

    public void setRuleProfileJson(String ruleProfileJson) {
        this.ruleProfileJson = ruleProfileJson;
    }

    public String getPreviewBaselineJson() {
        return previewBaselineJson;
    }

    public void setPreviewBaselineJson(String previewBaselineJson) {
        this.previewBaselineJson = previewBaselineJson;
    }

    public String getCompiledTemplatePath() {
        return compiledTemplatePath;
    }

    public void setCompiledTemplatePath(String compiledTemplatePath) {
        this.compiledTemplatePath = compiledTemplatePath;
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

