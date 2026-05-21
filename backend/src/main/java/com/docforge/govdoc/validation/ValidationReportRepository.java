package com.docforge.govdoc.validation;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ValidationReportRepository extends JpaRepository<ValidationReportEntity, String> {
    List<ValidationReportEntity> findByDocumentIdOrderByCreatedAtDesc(String documentId);
}

