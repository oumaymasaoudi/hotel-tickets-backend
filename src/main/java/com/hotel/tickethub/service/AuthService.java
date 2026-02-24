package com.hotel.tickethub.service;

import com.hotel.tickethub.dto.AuthResponse;
import com.hotel.tickethub.dto.LoginRequest;
import com.hotel.tickethub.dto.RegisterRequest;
import com.hotel.tickethub.exception.BusinessException;
import com.hotel.tickethub.exception.ConflictException;
import com.hotel.tickethub.exception.ResourceNotFoundException;
import com.hotel.tickethub.model.Hotel;
import com.hotel.tickethub.model.User;
import com.hotel.tickethub.model.UserRole;
import com.hotel.tickethub.repository.HotelRepository;
import com.hotel.tickethub.repository.UserRepository;
import com.hotel.tickethub.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String DEV_TOKEN = "dev-token";

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final HotelRepository hotelRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Security: Block SUPERADMIN creation via public interface
        if (request.getRole() != null && "SUPERADMIN".equalsIgnoreCase(request.getRole())) {
            throw new BusinessException(
                    "SuperAdmin role cannot be created via registration interface. Contact the developer.");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email already exists");
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

        // Business rule: Admin and Technician must be linked to a hotel
        assignHotelToUser(request, user);

        user = userRepository.save(user);

        // Create user role
        UserRole userRole = createUserRole(request, user);
        userRoleRepository.save(userRole);

        // Generate token
        return buildAuthResponse(user, userRole);
    }

    private void assignHotelToUser(RegisterRequest request, User user) {
        // Rule: "Un utilisateur (technicien ou admin) est rattaché à un hôtel via son HotelID"
        if (request.getRole() != null &&
                ("ADMIN".equalsIgnoreCase(request.getRole()) ||
                        "TECHNICIAN".equalsIgnoreCase(request.getRole()))) {
            if (request.getHotelId() == null || request.getHotelId().isEmpty()) {
                throw new IllegalArgumentException("An ADMIN or TECHNICIAN must be linked to a hotel");
            }
            Hotel hotel = hotelRepository.findById(UUID.fromString(request.getHotelId()))
                    .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));
            user.setHotel(hotel);
        } else if (request.getHotelId() != null && !request.getHotelId().isEmpty()) {
            // For other roles (CLIENT), hotel is optional
            Hotel hotel = hotelRepository.findById(UUID.fromString(request.getHotelId()))
                    .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));
            user.setHotel(hotel);
        }
    }

    private UserRole createUserRole(RegisterRequest request, User user) {
        UserRole userRole = new UserRole();
        userRole.setUser(user);
        userRole.setRole(request.getRole() != null
                ? com.hotel.tickethub.model.enums.UserRole.valueOf(request.getRole().toUpperCase())
                : com.hotel.tickethub.model.enums.UserRole.CLIENT);

        if (user.getHotel() != null) {
            userRole.setHotel(user.getHotel());
        }
        return userRole;
    }

    private AuthResponse buildAuthResponse(User user, UserRole userRole) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", userRole.getRole().name());
        claims.put("authorities", List.of("ROLE_" + userRole.getRole().name()));
        if (user.getHotel() != null) {
            claims.put("hotelId", user.getHotel().getId().toString());
        }

        // Token generation disabled, returns a dummy token
        return AuthResponse.builder()
                .token(DEV_TOKEN)
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
                .orElseThrow(() -> new IllegalArgumentException("User not found. Check your email."));

        // Rule 13: Check if account is locked
        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(LocalDateTime.now())) {
            throw new IllegalStateException("Account locked. Try again after " + user.getLockedUntil());
        }

        // If lock expired, reset
        if (user.getLockedUntil() != null && user.getLockedUntil().isBefore(LocalDateTime.now())) {
            user.setLockedUntil(null);
            user.setFailedLoginAttempts(0);
            userRepository.save(user);
        }

        // Try multiple methods to find user role (handles VARCHAR to UUID conversion
        // issue)
        UserRole userRole = userRoleRepository.findByUserId(user.getId())
                .or(() -> userRoleRepository.findByUserIdCustom(user.getId()))
                .or(() -> userRoleRepository.findByUserIdNative(user.getId()))
                .orElseThrow(() -> {
                    // Debug: Log the issue
                    log.debug("User ID: {}, User ID type: {}", user.getId(), user.getId().getClass().getName());
                    return new IllegalStateException(
                            "User role not found. Contact administrator. User ID: " + user.getId());
                });

        // Debug: Log password verification details
        String providedPassword = request.getPassword();
        String storedHash = user.getPassword();
        log.debug("Login attempt - Email: {}", user.getEmail());
        log.debug("Login attempt - Provided password length: {}",
                providedPassword != null ? providedPassword.length() : 0);
        log.debug("Login attempt - Stored hash length: {}", storedHash != null ? storedHash.length() : 0);
        log.debug("Login attempt - Stored hash start: {}",
                storedHash != null && storedHash.length() > 30 ? storedHash.substring(0, 30) : storedHash);

        // Password verification with BCrypt
        boolean passwordMatches = passwordEncoder.matches(providedPassword, storedHash);
        log.debug("Login attempt - Password matches: {}", passwordMatches);

        if (!passwordMatches) {
            // Rule 13: Increment failed attempts
            int attempts = (user.getFailedLoginAttempts() != null ? user.getFailedLoginAttempts() : 0) + 1;
            user.setFailedLoginAttempts(attempts);

            // Lock after 5 failed attempts (15 minutes)
            if (attempts >= 5) {
                user.setLockedUntil(LocalDateTime.now().plusMinutes(15));
                userRepository.save(user);
                throw new IllegalStateException("Account locked after 5 failed attempts. Try again in 15 minutes.");
            }

            userRepository.save(user);
            throw new IllegalArgumentException("Incorrect password. Remaining attempts: " + (5 - attempts));
        }

        // Successful login: reset attempts
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);

        // Token generation disabled, returns a dummy token
        return AuthResponse.builder()
                .token(DEV_TOKEN)
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
        if (roleName != null && "SUPERADMIN".equalsIgnoreCase(roleName)) {
            throw new BusinessException(
                    "SuperAdmin role cannot be assigned via this endpoint. Use SQL script or dedicated endpoint.");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

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
            throw new ConflictException("A SuperAdmin already exists. There can only be one SuperAdmin.");
        }

        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("A user with this email already exists");
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
                .token(DEV_TOKEN)
                .email(user.getEmail())
                .userId(user.getId().toString())
                .fullName(user.getFullName())
                .role(userRole.getRole().name())
                .hotelId(null) // SuperAdmin has no hotel
                .build();
    }
}
