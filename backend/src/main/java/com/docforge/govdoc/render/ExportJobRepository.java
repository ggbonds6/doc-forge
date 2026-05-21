package com.docforge.govdoc.render;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExportJobRepository extends JpaRepository<ExportJobEntity, String> {
    List<ExportJobEntity> findAllByOrderByCreatedAtDesc();
}

