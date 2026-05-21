package com.docforge.govdoc.validation;

public record ValidationIssue(
        ValidationSeverity severity,
        String code,
        String field,
        String message
) {
}

