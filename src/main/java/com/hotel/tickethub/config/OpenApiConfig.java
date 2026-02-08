package com.hotel.tickethub.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration OpenAPI (Swagger) pour la documentation de l'API
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI hotelTicketHubOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        
        return new OpenAPI()
                .info(new Info()
                        .title("Hotel Ticket Hub API")
                        .description("API REST complète pour la gestion de tickets d'hôtels. " +
                                "Cette API permet de gérer les hôtels, utilisateurs, tickets, paiements et abonnements.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Support Technique")
                                .email("support@tickethotel.com")
                                .url("https://tickethotel.com"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://tickethotel.com/license")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Serveur de développement local"),
                        new Server()
                                .url("http://13.63.15.86:8081")
                                .description("Serveur de staging")))
                .addSecurityItem(new SecurityRequirement()
                        .addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT token obtenu via /api/auth/login")));
    }
}
