package com.docforge.govdoc.template;

import com.docforge.govdoc.document.DocumentType;

import java.util.ArrayList;
import java.util.List;

public class ComplianceProfile {
    private List<DocumentType> allowedDocTypes = new ArrayList<>();
    private Boolean requireDocumentNumber = Boolean.TRUE;
    private Boolean requireSignerForRequest = Boolean.TRUE;
    private Boolean requirePrintFooter = Boolean.TRUE;
    private Boolean enforceTitleSuffix = Boolean.TRUE;

    public List<DocumentType> getAllowedDocTypes() {
        return allowedDocTypes;
    }

    public void setAllowedDocTypes(List<DocumentType> allowedDocTypes) {
        this.allowedDocTypes = allowedDocTypes;
    }

    public Boolean getRequireDocumentNumber() {
        return requireDocumentNumber;
    }

    public void setRequireDocumentNumber(Boolean requireDocumentNumber) {
        this.requireDocumentNumber = requireDocumentNumber;
    }

    public Boolean getRequireSignerForRequest() {
        return requireSignerForRequest;
    }

    public void setRequireSignerForRequest(Boolean requireSignerForRequest) {
        this.requireSignerForRequest = requireSignerForRequest;
    }

    public Boolean getRequirePrintFooter() {
        return requirePrintFooter;
    }

    public void setRequirePrintFooter(Boolean requirePrintFooter) {
        this.requirePrintFooter = requirePrintFooter;
    }

    public Boolean getEnforceTitleSuffix() {
        return enforceTitleSuffix;
    }

    public void setEnforceTitleSuffix(Boolean enforceTitleSuffix) {
        this.enforceTitleSuffix = enforceTitleSuffix;
    }
}

