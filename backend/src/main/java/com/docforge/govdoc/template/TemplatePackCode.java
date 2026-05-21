package com.docforge.govdoc.template;

import com.docforge.govdoc.document.DocumentType;

public enum TemplatePackCode {
    NOTICE(DocumentType.NOTICE, "通知"),
    REQUEST(DocumentType.REQUEST, "请示"),
    LETTER(DocumentType.LETTER, "函"),
    REPORT(DocumentType.REPORT, "报告");

    private final DocumentType documentType;
    private final String displayName;

    TemplatePackCode(DocumentType documentType, String displayName) {
        this.documentType = documentType;
        this.displayName = displayName;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public String getDisplayName() {
        return displayName;
    }
}

