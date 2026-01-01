package com.hotel.tickethub.config;

import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Configuration pour le rate limiting simple
 * Limite les requêtes pour éviter les abus
 * 100 requêtes par minute par IP
 */
@Configuration
public class RateLimitConfig {

    private static final int MAX_REQUESTS = 100;
    private static final long TIME_WINDOW_SECONDS = 60; // 1 minute
    
    private final Map<String, RequestCounter> cache = new ConcurrentHashMap<>();

    /**
     * Vérifier si une requête peut être traitée
     * @param ip Adresse IP du client
     * @return true si la requête peut être traitée, false sinon
     */
    public boolean tryConsume(String ip) {
        RequestCounter counter = cache.computeIfAbsent(ip, k -> new RequestCounter());
        
        LocalDateTime now = LocalDateTime.now();
        
        // Réinitialiser si la fenêtre de temps est expirée
        if (counter.getWindowStart().plusSeconds(TIME_WINDOW_SECONDS).isBefore(now)) {
            counter.reset(now);
        }
        
        // Vérifier la limite
        if (counter.getCount() >= MAX_REQUESTS) {
            return false;
        }
        
        // Incrémenter le compteur
        counter.increment();
        return true;
    }

    /**
     * Classe interne pour compter les requêtes
     */
    private static class RequestCounter {
        private int count = 0;
        private LocalDateTime windowStart = LocalDateTime.now();

        public void increment() {
            count++;
        }

        public void reset(LocalDateTime newStart) {
            count = 1;
            windowStart = newStart;
        }

        public int getCount() {
            return count;
        }

        public LocalDateTime getWindowStart() {
            return windowStart;
        }
    }
}

