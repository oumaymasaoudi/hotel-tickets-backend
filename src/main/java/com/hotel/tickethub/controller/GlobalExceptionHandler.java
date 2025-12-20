package com.hotel.tickethub.controller;

import com.stripe.exception.StripeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler to return JSON error messages
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String ERROR_KEY = "error";

    @ExceptionHandler(StripeException.class)
    public ResponseEntity<Map<String, String>> handleStripeException(StripeException e) {
        Map<String, String> error = new HashMap<>();
        error.put(ERROR_KEY, "Stripe error: " + e.getMessage());
        error.put("type", e.getClass().getSimpleName());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException e) {
        Map<String, String> error = new HashMap<>();

        // More specific error messages based on context
        String errorMessage = e.getMessage();
        if (errorMessage != null) {
            if (errorMessage.contains("Stripe") || errorMessage.contains("payment")
                    || errorMessage.contains("subscription")) {
                error.put(ERROR_KEY, errorMessage);
            } else if (errorMessage.contains("Plan") || errorMessage.contains("plan")) {
                error.put(ERROR_KEY, errorMessage);
            } else if (errorMessage.contains("Hotel") || errorMessage.contains("hotel")) {
                error.put(ERROR_KEY, errorMessage);
            } else {
                error.put(ERROR_KEY, "Error: " + errorMessage);
            }
        } else {
            error.put(ERROR_KEY, "An error occurred");
        }

        error.put("message", errorMessage);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
