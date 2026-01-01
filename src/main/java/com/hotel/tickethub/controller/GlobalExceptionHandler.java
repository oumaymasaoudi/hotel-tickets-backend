package com.hotel.tickethub.controller;

import com.stripe.exception.StripeException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler to return JSON error messages with appropriate HTTP status codes
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String ERROR_KEY = "error";
    private static final String MESSAGE_KEY = "message";

    @ExceptionHandler(StripeException.class)
    public ResponseEntity<Map<String, Object>> handleStripeException(StripeException e) {
        Map<String, Object> error = new HashMap<>();
        error.put(ERROR_KEY, "Erreur de paiement");
        error.put(MESSAGE_KEY, "Erreur Stripe: " + e.getMessage());
        error.put("type", e.getClass().getSimpleName());
        error.put("code", "STRIPE_ERROR");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(MethodArgumentNotValidException e) {
        Map<String, Object> error = new HashMap<>();
        error.put(ERROR_KEY, "Erreur de validation");
        error.put("code", "VALIDATION_ERROR");

        Map<String, String> fieldErrors = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach((err) -> {
            String fieldName = ((FieldError) err).getField();
            String errorMessage = err.getDefaultMessage();
            fieldErrors.put(fieldName, errorMessage != null ? errorMessage : "Valeur invalide");
        });
        error.put("fields", fieldErrors);
        error.put(MESSAGE_KEY, "Les données fournies ne sont pas valides");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolationException(ConstraintViolationException e) {
        Map<String, Object> error = new HashMap<>();
        error.put(ERROR_KEY, "Erreur de validation");
        error.put("code", "CONSTRAINT_VIOLATION");

        Map<String, String> violations = e.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        v -> v.getPropertyPath().toString(),
                        ConstraintViolation::getMessage
                ));
        error.put("violations", violations);
        error.put(MESSAGE_KEY, "Les contraintes de validation ne sont pas respectées");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException e) {
        Map<String, Object> error = new HashMap<>();
        error.put(ERROR_KEY, "Paramètre invalide");
        error.put(MESSAGE_KEY, e.getMessage());
        error.put("code", "INVALID_ARGUMENT");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String, Object>> handleSecurityException(SecurityException e) {
        Map<String, Object> error = new HashMap<>();
        error.put(ERROR_KEY, "Erreur de sécurité");
        error.put(MESSAGE_KEY, e.getMessage());
        error.put("code", "SECURITY_ERROR");

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException e) {
        Map<String, Object> error = new HashMap<>();
        String errorMessage = e.getMessage();

        // Determine HTTP status and error code based on error message
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String errorCode = "GENERIC_ERROR";

        if (errorMessage != null) {
            // Not found errors
            if (errorMessage.contains("not found") || errorMessage.contains("Not found")) {
                status = HttpStatus.NOT_FOUND;
                errorCode = "NOT_FOUND";
                error.put(ERROR_KEY, "Ressource non trouvée");
            }
            // Already exists errors
            else if (errorMessage.contains("already exists") || errorMessage.contains("déjà existe")) {
                status = HttpStatus.CONFLICT;
                errorCode = "ALREADY_EXISTS";
                error.put(ERROR_KEY, "Ressource déjà existante");
            }
            // Authentication/Authorization errors
            else if (errorMessage.contains("unauthorized") || errorMessage.contains("forbidden")
                    || errorMessage.contains("Access denied")) {
                status = HttpStatus.FORBIDDEN;
                errorCode = "ACCESS_DENIED";
                error.put(ERROR_KEY, "Accès refusé");
            }
            // Payment/Stripe errors
            else if (errorMessage.contains("Stripe") || errorMessage.contains("payment")
                    || errorMessage.contains("subscription")) {
                errorCode = "PAYMENT_ERROR";
                error.put(ERROR_KEY, "Erreur de paiement");
            }
            // Plan errors
            else if (errorMessage.contains("Plan") || errorMessage.contains("plan")) {
                errorCode = "PLAN_ERROR";
                error.put(ERROR_KEY, "Erreur liée au plan");
            }
            // Hotel errors
            else if (errorMessage.contains("Hotel") || errorMessage.contains("hotel")) {
                errorCode = "HOTEL_ERROR";
                error.put(ERROR_KEY, "Erreur liée à l'hôtel");
            }
            // Generic error
            else {
                error.put(ERROR_KEY, "Une erreur est survenue");
            }
        } else {
            error.put(ERROR_KEY, "Une erreur inattendue est survenue");
        }

        error.put(MESSAGE_KEY, errorMessage);
        error.put("code", errorCode);

        return ResponseEntity.status(status).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception e) {
        Map<String, Object> error = new HashMap<>();
        error.put(ERROR_KEY, "Erreur serveur");
        error.put(MESSAGE_KEY, "Une erreur inattendue est survenue. Veuillez réessayer plus tard.");
        error.put("code", "INTERNAL_SERVER_ERROR");

        // Log the full exception for debugging (in production, use proper logging)
        e.printStackTrace();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
