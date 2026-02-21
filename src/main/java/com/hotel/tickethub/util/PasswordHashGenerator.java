package com.hotel.tickethub.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utility class to generate BCrypt password hashes
 * Run this main method to generate a hash for a password
 * 
 * WARNING: This is a development utility only. Do not commit passwords.
 */
@Slf4j
public class PasswordHashGenerator {
    public static void main(String[] args) {
        if (args.length == 0) {
            log.error("Usage: PasswordHashGenerator <password>");
            log.error("WARNING: This utility is for development only. Never commit passwords.");
            return;
        }
        
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String password = args[0];
        String hash = encoder.encode(password);
        log.info("Password: {}", password);
        log.info("BCrypt Hash: {}", hash);

        // Verify the hash
        boolean matches = encoder.matches(password, hash);
        log.info("Verification: {}", matches ? "SUCCESS" : "FAILED");
    }
}
