package com.docforge.govdoc.validation;

import java.util.ArrayList;
import java.util.List;

public class ValidationReport {
    private boolean pass;
    private List<ValidationIssue> issues = new ArrayList<>();

    public boolean isPass() {
        return pass;
    }

    public void setPass(boolean pass) {
        this.pass = pass;
    }

    public List<ValidationIssue> getIssues() {
        return issues;
    }

    public void setIssues(List<ValidationIssue> issues) {
        this.issues = issues;
    }
}

