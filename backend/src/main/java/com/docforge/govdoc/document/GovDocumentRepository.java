package com.docforge.govdoc.document;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GovDocumentRepository extends JpaRepository<GovDocumentEntity, String> {
}

