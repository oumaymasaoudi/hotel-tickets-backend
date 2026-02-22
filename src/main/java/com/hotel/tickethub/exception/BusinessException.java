package com.hotel.tickethub.exception;

/**
 * Exception métier pour les erreurs de logique applicative
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}
