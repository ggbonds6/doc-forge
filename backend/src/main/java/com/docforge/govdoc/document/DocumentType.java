package com.docforge.govdoc.document;

public enum DocumentType {
    NOTICE("通知"),
    REQUEST("请示"),
    LETTER("函"),
    REPORT("报告");

    private final String displayName;

    DocumentType(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}

