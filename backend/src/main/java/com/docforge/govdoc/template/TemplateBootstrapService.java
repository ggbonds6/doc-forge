package com.docforge.govdoc.template;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class TemplateBootstrapService {
    private final TemplateService templateService;

    public TemplateBootstrapService(TemplateService templateService) {
        this.templateService = templateService;
    }

    @PostConstruct
    public void init() {
        templateService.ensureBuiltIns();
    }
}

