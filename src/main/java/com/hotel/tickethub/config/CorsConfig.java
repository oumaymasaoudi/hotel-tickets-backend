package com.hotel.tickethub.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration CORS supplémentaire pour les endpoints non sécurisés.
 * La configuration principale est dans SecurityConfig.java
 */
@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        // ✅ Spécifier les origines explicitement (pas de wildcard avec
                        // allowCredentials)
                        .allowedOrigins(
                                "http://localhost:5173",
                                "http://localhost:3000",
                                "http://localhost:8080",
                                "http://localhost:8081",
                                "http://192.168.58.1:5173")
                        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .exposedHeaders("*")
                        .allowCredentials(true)
                        .maxAge(3600); // 1 heure
            }
        };
    }
}
