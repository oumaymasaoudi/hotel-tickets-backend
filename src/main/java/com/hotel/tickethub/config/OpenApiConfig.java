package com.hotel.tickethub.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
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
        return new OpenAPI()
                .info(new Info()
                        .title("Hotel Ticket Hub API")
                        .description("API REST pour la gestion de tickets d'hôtels")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Support")
                                .email("support@tickethotel.com"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://tickethotel.com")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("Serveur de développement"),
                        new Server()
                                .url("http://13.49.44.219:8081")
                                .description("Serveur de staging")));
    }
}

