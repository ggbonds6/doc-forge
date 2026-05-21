package com.docforge.govdoc.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "docforge.admin")
public record AdminAuthProperties(
        String username,
        String password,
        String token
) {
}

