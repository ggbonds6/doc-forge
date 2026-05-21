package com.docforge.govdoc.template;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class TemplatePreviewBaseline {
    private String standardRef = "GB/T 9704-2012《党政机关公文格式》";
    private String documentType;
    private String compiledTemplatePath;
    private Instant compiledAt;
    private String reviewStatus = "PENDING_WPS_REVIEW";
    private String publishOverrideReason;
    private String publishOverrideReviewer;
    private Instant publishedAt;
    private List<TemplateAcceptanceCriterion> criteria = new ArrayList<>();

    public String getStandardRef() {
        return standardRef;
    }

    public void setStandardRef(String standardRef) {
        this.standardRef = standardRef;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getCompiledTemplatePath() {
        return compiledTemplatePath;
    }

    public void setCompiledTemplatePath(String compiledTemplatePath) {
        this.compiledTemplatePath = compiledTemplatePath;
    }

    public Instant getCompiledAt() {
        return compiledAt;
    }

    public void setCompiledAt(Instant compiledAt) {
        this.compiledAt = compiledAt;
    }

    public String getReviewStatus() {
        return reviewStatus;
    }

    public void setReviewStatus(String reviewStatus) {
        this.reviewStatus = reviewStatus;
    }

    public String getPublishOverrideReason() {
        return publishOverrideReason;
    }

    public void setPublishOverrideReason(String publishOverrideReason) {
        this.publishOverrideReason = publishOverrideReason;
    }

    public String getPublishOverrideReviewer() {
        return publishOverrideReviewer;
    }

    public void setPublishOverrideReviewer(String publishOverrideReviewer) {
        this.publishOverrideReviewer = publishOverrideReviewer;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(Instant publishedAt) {
        this.publishedAt = publishedAt;
    }

    public List<TemplateAcceptanceCriterion> getCriteria() {
        return criteria;
    }

    public void setCriteria(List<TemplateAcceptanceCriterion> criteria) {
        this.criteria = criteria;
    }
}
