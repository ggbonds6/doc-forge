package com.docforge.govdoc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class GovDocApplication {

    public static void main(String[] args) {
        SpringApplication.run(GovDocApplication.class, args);
    }
}

