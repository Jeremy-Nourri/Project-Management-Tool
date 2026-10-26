package com.pmt.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Autorise le frontend Angular a appeler l'API depuis une autre origine.
 * Les origines acceptees sont configurables par la variable CORS_ORIGINS.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private final String[] originesAutorisees;

    public CorsConfig(@Value("${pmt.cors.origines:http://localhost:4200}") String[] originesAutorisees) {
        this.originesAutorisees = originesAutorisees;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(originesAutorisees)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600);
    }
}
