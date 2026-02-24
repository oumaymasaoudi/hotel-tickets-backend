package com.hotel.tickethub.exception;

/**
 * Exception pour les erreurs de stockage de fichiers
 */
public class StorageException extends RuntimeException {
    public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
