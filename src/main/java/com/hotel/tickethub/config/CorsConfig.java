package com.hotel.tickethub.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Configuration CORS supplémentaire pour les endpoints non sécurisés.
 * La configuration principale est dans SecurityConfig.java
 */
@Configuration
public class CorsConfig {

    @Value("${cors.allowed-origins:}")
    private String corsAllowedOrigins;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                // Build allowed origins list
                List<String> allowedOrigins = new ArrayList<>(Arrays.asList(
                        "http://localhost:5173",
                        "http://localhost:3000",
                        "http://localhost:8080",
                        "http://localhost:8081",
                        "http://192.168.58.1:5173",
                        // Frontend staging URLs (with common ports)
                        "http://13.50.221.51",
                        "http://13.50.221.51:80",
                        "http://13.50.221.51:8080",
                        "https://13.50.221.51",
                        "https://13.50.221.51:443"));

                // Add origins from environment variable if provided
                if (corsAllowedOrigins != null && !corsAllowedOrigins.trim().isEmpty()) {
                    String[] origins = corsAllowedOrigins.split(",");
                    for (String origin : origins) {
                        String trimmed = origin.trim();
                        if (!trimmed.isEmpty() && !allowedOrigins.contains(trimmed)) {
                            allowedOrigins.add(trimmed);
                        }
                    }
                }

                registry.addMapping("/**")
                        // Specify origins explicitly (no wildcard with allowCredentials)
                        .allowedOrigins(allowedOrigins.toArray(new String[0]))
                        .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                        .allowedHeaders("*")
                        .exposedHeaders("*")
                        .allowCredentials(true)
                        .maxAge(3600); // 1 heure
            }
        };
    }
}
