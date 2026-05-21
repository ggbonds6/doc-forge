package com.docforge.govdoc.document;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GovDocumentVersionRepository extends JpaRepository<GovDocumentVersionEntity, String> {
    Optional<GovDocumentVersionEntity> findFirstByDocumentIdOrderByVersionNoDesc(String documentId);
}

