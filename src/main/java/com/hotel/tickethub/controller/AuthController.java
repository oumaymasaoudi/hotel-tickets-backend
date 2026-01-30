package com.hotel.tickethub.controller;

import com.hotel.tickethub.dto.AuthResponse;
import com.hotel.tickethub.dto.LoginRequest;
import com.hotel.tickethub.dto.RegisterRequest;
import com.hotel.tickethub.model.User;
import com.hotel.tickethub.repository.UserRepository;
import com.hotel.tickethub.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = {
        "http://localhost:8080",
        "http://localhost:8081",
        "http://localhost:5173",
        "http://192.168.58.1:5173"
})
public class AuthController {

    private static final String ERROR_KEY = "error";

    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<Object> register(@Valid @RequestBody RegisterRequest request) {
        try {
            return ResponseEntity.ok(authService.register(request));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // Return error message in JSON format for frontend
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            errorResponse.put(ERROR_KEY, "Login failed");
            return ResponseEntity.status(400)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorResponse);
        }
    }

    // Temporary: Endpoint to fix user role
    @PostMapping("/fix-role")
    public ResponseEntity<String> fixUserRole(@RequestParam String email, @RequestParam String role) {
        authService.updateUserRole(email, role);
        return ResponseEntity.ok("Role updated successfully for " + email);
    }

    /**
     * Development endpoint only
     * Create a SuperAdmin (developer only)
     * 
     * Disable in production!
     * 
     * Usage:
     * POST /api/auth/create-superadmin?secret=DEV_SECRET_KEY
     * Body: {
     * "email": "superadmin@example.com",
     * "password": "SecurePassword123!",
     * "fullName": "Super Admin"
     * }
     */
    @PostMapping("/create-superadmin")
    public ResponseEntity<Object> createSuperAdmin(
            @RequestParam(required = false) String secret,
            @RequestBody Map<String, String> request) {

        // Basic protection - Strengthen in production or disable completely
        String devSecret = System.getenv("DEV_SECRET_KEY");
        if (devSecret == null) {
            devSecret = "DEV_SECRET_CHANGE_IN_PRODUCTION"; // Default value for dev
        }

        if (secret == null || !secret.equals(devSecret)) {
            return ResponseEntity.status(403).body("Access denied. Secret key required.");
        }

        try {
            String email = request.get("email");
            String password = request.get("password");
            String fullName = request.get("fullName");

            if (email == null || password == null || fullName == null) {
                return ResponseEntity.status(400).body("Email, password and fullName are required");
            }

            AuthResponse response = authService.createSuperAdmin(email, password, fullName);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", e.getMessage());
            errorResponse.put(ERROR_KEY, "Failed to create SuperAdmin");
            return ResponseEntity.status(400)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(errorResponse);
        }
    }

    /**
     * Temporary endpoint to generate BCrypt hash for a password
     * Usage: GET /api/auth/generate-hash?password=admin123
     */
    @GetMapping("/generate-hash")
    public ResponseEntity<Map<String, String>> generateHash(@RequestParam String password) {
        String hash = passwordEncoder.encode(password);
        Map<String, String> response = new HashMap<>();
        response.put("password", password);
        response.put("hash", hash);
        response.put("sql", "UPDATE users SET password = '" + hash + "' WHERE email = 'oumaymasaoudi6@gmail.com';");
        return ResponseEntity.ok(response);
    }

    /**
     * Test endpoint to verify password matching
     * Usage: GET /api/auth/test-password?password=admin123
     */
    @GetMapping("/test-password")
    public ResponseEntity<Map<String, Object>> testPassword(@RequestParam String password) {
        Map<String, Object> response = new HashMap<>();

        // Get user from database
        User user = userRepository.findByEmail("oumaymasaoudi6@gmail.com")
                .orElse(null);

        if (user == null) {
            response.put("error", "User not found");
            return ResponseEntity.ok(response);
        }

        String storedHash = user.getPassword();
        boolean matches = passwordEncoder.matches(password, storedHash);

        response.put("email", user.getEmail());
        response.put("password_provided", password);
        response.put("password_length", password.length());
        response.put("stored_hash", storedHash);
        response.put("hash_length", storedHash != null ? storedHash.length() : 0);
        response.put("matches", matches);

        return ResponseEntity.ok(response);
    }
}
