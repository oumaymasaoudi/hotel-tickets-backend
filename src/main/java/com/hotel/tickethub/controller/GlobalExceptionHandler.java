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
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Global exception handler to return JSON error messages with appropriate HTTP status codes
 * Excludes Actuator endpoints to allow Spring Boot Actuator to handle its own errors
 */
@RestControllerAdvice(basePackages = "com.hotel.tickethub.controller")
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
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException e, WebRequest request) {
        // Exclude Actuator endpoints from global exception handling
        if (request instanceof ServletWebRequest servletWebRequest) {
            String path = servletWebRequest.getRequest().getRequestURI();
            if (path != null && path.startsWith("/actuator")) {
                // Let Spring Boot Actuator handle its own errors
                throw e;
            }
        }
        
        Map<String, Object> error = new HashMap<>();
        String errorMessage = e.getMessage();
        ErrorInfo errorInfo = determineErrorInfo(errorMessage);
        
        error.put(ERROR_KEY, errorInfo.errorMessage);
        error.put(MESSAGE_KEY, errorMessage);
        error.put("code", errorInfo.errorCode);

        return ResponseEntity.status(errorInfo.status).body(error);
    }
    
    private record ErrorInfo(HttpStatus status, String errorCode, String errorMessage) {}
    
    private ErrorInfo determineErrorInfo(String errorMessage) {
        if (errorMessage == null) {
            return new ErrorInfo(HttpStatus.BAD_REQUEST, "GENERIC_ERROR", "Une erreur inattendue est survenue");
        }
        
        // Not found errors
        if (errorMessage.contains("not found") || errorMessage.contains("Not found")) {
            return new ErrorInfo(HttpStatus.NOT_FOUND, "NOT_FOUND", "Ressource non trouvée");
        }
        // Already exists errors
        if (errorMessage.contains("already exists") || errorMessage.contains("déjà existe")) {
            return new ErrorInfo(HttpStatus.CONFLICT, "ALREADY_EXISTS", "Ressource déjà existante");
        }
        // Authentication/Authorization errors
        if (errorMessage.contains("unauthorized") || errorMessage.contains("forbidden")
                || errorMessage.contains("Access denied")) {
            return new ErrorInfo(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "Accès refusé");
        }
        // Payment/Stripe errors
        if (errorMessage.contains("Stripe") || errorMessage.contains("payment")
                || errorMessage.contains("subscription")) {
            return new ErrorInfo(HttpStatus.BAD_REQUEST, "PAYMENT_ERROR", "Erreur de paiement");
        }
        // Plan errors
        if (errorMessage.contains("Plan") || errorMessage.contains("plan")) {
            return new ErrorInfo(HttpStatus.BAD_REQUEST, "PLAN_ERROR", "Erreur liée au plan");
        }
        // Hotel errors
        if (errorMessage.contains("Hotel") || errorMessage.contains("hotel")) {
            return new ErrorInfo(HttpStatus.BAD_REQUEST, "HOTEL_ERROR", "Erreur liée à l'hôtel");
        }
        // Generic error
        return new ErrorInfo(HttpStatus.BAD_REQUEST, "GENERIC_ERROR", "Une erreur est survenue");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception e, WebRequest request) {
        // Exclude Actuator endpoints from global exception handling
        if (request instanceof ServletWebRequest servletWebRequest) {
            String path = servletWebRequest.getRequest().getRequestURI();
            if (path != null && path.startsWith("/actuator")) {
                // Let Spring Boot Actuator handle its own errors
                throw new IllegalStateException("Actuator endpoint error", e);
            }
        }
        
        Map<String, Object> error = new HashMap<>();
        error.put(ERROR_KEY, "Erreur serveur");
        error.put(MESSAGE_KEY, "Une erreur inattendue est survenue. Veuillez réessayer plus tard.");
        error.put("code", "INTERNAL_SERVER_ERROR");

        // Log the full exception for debugging (in production, use proper logging)
        e.printStackTrace();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
