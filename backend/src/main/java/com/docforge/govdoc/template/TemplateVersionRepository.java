package com.docforge.govdoc.template;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TemplateVersionRepository extends JpaRepository<TemplateVersionEntity, String> {
    List<TemplateVersionEntity> findByTemplatePackIdOrderByCreatedAtDesc(String templatePackId);

    Optional<TemplateVersionEntity> findFirstByTemplatePackIdAndStatusOrderByUpdatedAtDesc(String templatePackId, String status);
}

