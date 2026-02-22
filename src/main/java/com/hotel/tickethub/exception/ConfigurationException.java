package com.hotel.tickethub.exception;

/**
 * Exception pour les erreurs de configuration
 */
public class ConfigurationException extends RuntimeException {
    public ConfigurationException(String message) {
        super(message);
    }

    public ConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
