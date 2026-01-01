package com.hotel.tickethub.service;

import com.hotel.tickethub.dto.AuthResponse;
import com.hotel.tickethub.dto.LoginRequest;
import com.hotel.tickethub.dto.RegisterRequest;
import com.hotel.tickethub.model.Hotel;
import com.hotel.tickethub.model.User;
import com.hotel.tickethub.model.UserRole;
import com.hotel.tickethub.repository.HotelRepository;
import com.hotel.tickethub.repository.UserRepository;
import com.hotel.tickethub.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final HotelRepository hotelRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Security: Block SUPERADMIN creation via public interface
        if (request.getRole() != null && request.getRole().toUpperCase().equals("SUPERADMIN")) {
            throw new RuntimeException(
                    "SuperAdmin role cannot be created via registration interface. Contact the developer.");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setIsActive(true);

        // Add specialties for technicians
        if (request.getSpecialties() != null && !request.getSpecialties().isEmpty()) {
            user.setSpecialties(request.getSpecialties());
        }

        // Business rule: Admin must be linked to a hotel
        // Technicians work for all hotels, so they don't need a hotelId
        if (request.getRole() != null && request.getRole().toUpperCase().equals("ADMIN")) {
            if (request.getHotelId() == null || request.getHotelId().isEmpty()) {
                throw new RuntimeException("An ADMIN must be linked to a hotel");
            }
            Hotel hotel = hotelRepository.findById(UUID.fromString(request.getHotelId()))
                    .orElseThrow(() -> new RuntimeException("Hotel not found"));
            user.setHotel(hotel);
        } else if (request.getHotelId() != null && !request.getHotelId().isEmpty()) {
            // For other roles (CLIENT, TECHNICIAN), hotel is optional
            // Technicians work for all hotels, so hotelId is not required
            Hotel hotel = hotelRepository.findById(UUID.fromString(request.getHotelId()))
                    .orElseThrow(() -> new RuntimeException("Hotel not found"));
            user.setHotel(hotel);
        }
        // Technicians without hotelId: user.setHotel(null) - they work for all hotels

        user = userRepository.save(user);

        // Create user role
        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(request.getRole() != null
                ? com.hotel.tickethub.model.enums.UserRole.valueOf(request.getRole().toUpperCase())
                : com.hotel.tickethub.model.enums.UserRole.CLIENT);

        if (user.getHotel() != null) {
            userRole.setHotel(user.getHotel());
        }

        userRoleRepository.save(userRole);

        // Generate token
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", userRole.getRole().name());
        claims.put("authorities", List.of("ROLE_" + userRole.getRole().name()));
        if (user.getHotel() != null) {
            claims.put("hotelId", user.getHotel().getId().toString());
        }

        // Token generation disabled, returns a dummy token
        return AuthResponse.builder()
                .token("dev-token")
                .email(user.getEmail())
                .userId(user.getId().toString())
                .fullName(user.getFullName())
                .role(userRole.getRole().name())
                .hotelId(user.getHotel() != null ? user.getHotel().getId().toString() : null)
                .build();
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found. Check your email."));

        // Rule 13: Check if account is locked
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new RuntimeException("Account locked. Try again after " + user.getLockedUntil());
        }

        // If lock expired, reset
        if (user.getLockedUntil() != null && user.getLockedUntil().isBefore(LocalDateTime.now())) {
            user.setLockedUntil(null);
            user.setFailedLoginAttempts(0);
            userRepository.save(user);
        }

        UserRole userRole = userRoleRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("User role not found. Contact administrator."));

        // Password verification with BCrypt
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            // Rule 13: Increment failed attempts
            int attempts = (user.getFailedLoginAttempts() != null ? user.getFailedLoginAttempts() : 0) + 1;
            user.setFailedLoginAttempts(attempts);

            // Lock after 5 failed attempts (15 minutes)
            if (attempts >= 5) {
                user.setLockedUntil(LocalDateTime.now().plusMinutes(15));
                userRepository.save(user);
                throw new RuntimeException("Account locked after 5 failed attempts. Try again in 15 minutes.");
            }

            userRepository.save(user);
            throw new RuntimeException("Incorrect password. Remaining attempts: " + (5 - attempts));
        }

        // Successful login: reset attempts
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);

        // Token generation disabled, returns a dummy token
        return AuthResponse.builder()
                .token("dev-token")
                .email(user.getEmail())
                .userId(user.getId().toString())
                .fullName(user.getFullName())
                .role(userRole.getRole().name())
                .hotelId(user.getHotel() != null ? user.getHotel().getId().toString() : null)
                .build();
    }

    @Transactional
    public void updateUserRole(String email, String roleName) {
        // Security: Block promotion to SUPERADMIN via this endpoint
        if (roleName != null && roleName.toUpperCase().equals("SUPERADMIN")) {
            throw new RuntimeException(
                    "SuperAdmin role cannot be assigned via this endpoint. Use SQL script or dedicated endpoint.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        UserRole userRole = userRoleRepository.findByUserId(user.getId())
                .orElse(new UserRole());

        userRole.setUser(user);
        userRole.setRole(com.hotel.tickethub.model.enums.UserRole.valueOf(roleName.toUpperCase()));
        userRoleRepository.save(userRole);
    }

    /**
     * Special method to create a SuperAdmin (developer only)
     * This method must be called manually or via a protected endpoint
     * 
     * @param email    SuperAdmin email
     * @param password Password
     * @param fullName Full name
     * @return AuthResponse with token
     */
    @Transactional
    public AuthResponse createSuperAdmin(String email, String password, String fullName) {
        // Check if SuperAdmin already exists
        boolean superAdminExists = userRoleRepository.findAll().stream()
                .anyMatch(ur -> ur.getRole() == com.hotel.tickethub.model.enums.UserRole.SUPERADMIN);

        if (superAdminExists) {
            throw new RuntimeException("A SuperAdmin already exists. There can only be one SuperAdmin.");
        }

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("A user with this email already exists");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFullName(fullName);
        user.setIsActive(true);

        user = userRepository.save(user);

        // Create SuperAdmin role
        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(com.hotel.tickethub.model.enums.UserRole.SUPERADMIN);
        // SuperAdmin is not linked to a specific hotel
        userRoleRepository.save(userRole);

        return AuthResponse.builder()
                .token("dev-token")
                .email(user.getEmail())
                .userId(user.getId().toString())
                .fullName(user.getFullName())
                .role(userRole.getRole().name())
                .hotelId(null) // SuperAdmin has no hotel
                .build();
    }
}
