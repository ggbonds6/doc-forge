package com.docforge.govdoc.template;

import java.time.Instant;

public class TemplateAcceptanceCriterion {
    private String code;
    private String name;
    private String description;
    private String status = "PENDING";
    private boolean blocking = true;
    private String reviewer;
    private String comment;
    private Instant reviewedAt;

    public TemplateAcceptanceCriterion() {
    }

    public TemplateAcceptanceCriterion(String code, String name, String description, boolean blocking) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.blocking = blocking;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isBlocking() {
        return blocking;
    }

    public void setBlocking(boolean blocking) {
        this.blocking = blocking;
    }

    public String getReviewer() {
        return reviewer;
    }

    public void setReviewer(String reviewer) {
        this.reviewer = reviewer;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Instant getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(Instant reviewedAt) {
        this.reviewedAt = reviewedAt;
    }
}
