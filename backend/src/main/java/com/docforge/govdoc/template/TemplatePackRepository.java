package com.docforge.govdoc.template;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TemplatePackRepository extends JpaRepository<TemplatePackEntity, String> {
    Optional<TemplatePackEntity> findByCode(String code);
}

