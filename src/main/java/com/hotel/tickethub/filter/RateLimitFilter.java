package com.hotel.tickethub.filter;

import com.hotel.tickethub.config.RateLimitConfig;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtre pour le rate limiting
 * Limite le nombre de requêtes par IP
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitConfig rateLimitConfig;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String clientIp = getClientIpAddress(request);

        // Exclure localhost du rate limiting (pour le développement)
        if (clientIp.equals("127.0.0.1") || clientIp.equals("localhost") ||
                clientIp.equals("0:0:0:0:0:0:0:1") || clientIp.startsWith("192.168.") ||
                clientIp.startsWith("10.") || clientIp.startsWith("172.16.")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Exclure les endpoints publics de documentation et d'inscription
        String path = request.getRequestURI();
        if (path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs") ||
                path.startsWith("/actuator") ||
                path.startsWith("/api/hotels/public") ||
                path.startsWith("/api/categories/public") ||
                path.startsWith("/api/auth/register") ||
                path.startsWith("/api/auth/login") ||
                path.startsWith("/api/tickets/public")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!rateLimitConfig.tryConsume(clientIp)) {
            log.warn("Rate limit exceeded for IP: {}", clientIp);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"error\":\"Too many requests. Please try again later.\",\"code\":\"RATE_LIMIT_EXCEEDED\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
