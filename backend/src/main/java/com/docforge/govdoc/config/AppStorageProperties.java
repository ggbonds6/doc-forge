package com.docforge.govdoc.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "docforge.storage")
public record AppStorageProperties(String root) {
}

