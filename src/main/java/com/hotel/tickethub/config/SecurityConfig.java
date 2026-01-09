package com.hotel.tickethub.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import com.hotel.tickethub.filter.RateLimitFilter;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final RateLimitFilter rateLimitFilter;

    public SecurityConfig(RateLimitFilter rateLimitFilter) {
        this.rateLimitFilter = rateLimitFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Désactiver CSRF pour les API REST
                .csrf(csrf -> csrf.disable())

                // ✅ Configuration CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // ✅ Ajouter le rate limiting filter
                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)

                // Autoriser toutes les requêtes (pour le moment)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        return http.build();
    }

    // ✅ Configuration CORS pour autoriser les requêtes depuis le frontend
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // ✅ Autoriser les origines du frontend
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173",
                "http://localhost:3000",
                "http://localhost:8080",
                "http://localhost:8081",
                "http://192.168.58.1:5173",
                "http://13.50.221.51"));

        // ✅ Autoriser toutes les méthodes HTTP
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // ✅ Autoriser tous les headers
        configuration.setAllowedHeaders(List.of("*"));

        // ✅ Autoriser l'envoi de credentials (cookies, headers d'authentification)
        configuration.setAllowCredentials(true);

        // ✅ Exposer les headers dans la réponse
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type"));

        // ✅ Durée de validité du preflight request (1 heure)
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
