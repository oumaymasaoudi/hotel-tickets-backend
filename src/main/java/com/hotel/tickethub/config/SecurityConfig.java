package com.hotel.tickethub.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
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
import com.hotel.tickethub.security.JwtAuthenticationFilter;
import com.hotel.tickethub.security.JwtTokenProvider;
import com.hotel.tickethub.security.CustomUserDetailsService;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

        private final RateLimitFilter rateLimitFilter;
        private final JwtTokenProvider jwtTokenProvider;
        private final CustomUserDetailsService userDetailsService;

        @Value("${cors.allowed-origins:}")
        private String corsAllowedOrigins;

        public SecurityConfig(RateLimitFilter rateLimitFilter,
                        JwtTokenProvider jwtTokenProvider,
                        CustomUserDetailsService userDetailsService) {
                this.rateLimitFilter = rateLimitFilter;
                this.jwtTokenProvider = jwtTokenProvider;
                this.userDetailsService = userDetailsService;
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
                // Create JWT filter
                JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(
                                jwtTokenProvider,
                                userDetailsService);

                http
                                // Disable CSRF for REST API
                                .csrf(csrf -> csrf.disable())

                                // CORS configuration
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                                // Security headers
                                .headers(headers -> headers
                                                // Note: FrameOptionsConfig::deny n'est pas disponible dans cette version de Spring Security
                                                .frameOptions(frameOptions -> frameOptions.deny())
                                                .contentTypeOptions(contentTypeOptions -> {
                                                })
                                                .httpStrictTransportSecurity(hsts -> hsts
                                                                .maxAgeInSeconds(31536000)))

                                // Add rate limiting filter
                                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)

                                // Add JWT filter before default authentication filter
                                // Filter will authenticate if token present, but won't block if absent
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)

                                // Allow all requests (JWT filter handles auth if token present)
                                // @PreAuthorize in controllers will do the real verification
                                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

                return http.build();
        }

        // CORS configuration for frontend requests
        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();

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

                configuration.setAllowedOrigins(allowedOrigins);

                // Allowed HTTP methods
                configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

                // Allowed headers only
                configuration.setAllowedHeaders(Arrays.asList(
                                "Authorization",
                                "Content-Type",
                                "X-User-Email", // Only for dev-token in development
                                "X-Requested-With",
                                "Accept",
                                "Origin"));

                // Allow credentials (cookies, auth headers)
                configuration.setAllowCredentials(true);

                // Exposed headers in response
                configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type", "X-User-Email"));

                // Preflight request cache duration (1 hour)
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
