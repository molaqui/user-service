package com.bankati.userservice.config;

import jakarta.servlet.MultipartConfigElement;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.unit.DataSize;


@Configuration
public class MultipartConfig {
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize(DataSize.parse("10MB")); // Taille maximale des fichiers
        factory.setMaxRequestSize(DataSize.parse("10MB")); // Taille maximale de la requête
        return factory.createMultipartConfig();
    }
}
