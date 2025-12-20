package com.hotel.tickethub.controller;

import com.hotel.tickethub.dto.UserRequest;
import com.hotel.tickethub.dto.UserResponse;
import com.hotel.tickethub.model.User;
import com.hotel.tickethub.model.UserRole;
import com.hotel.tickethub.model.Hotel;
import com.hotel.tickethub.repository.UserRepository;
import com.hotel.tickethub.repository.UserRoleRepository;
import com.hotel.tickethub.repository.HotelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = { "http://localhost:8080", "http://localhost:8081", "http://localhost:5173" })
public class UserRestController {

    private static final String EMAIL_KEY = "email";
    private static final String FULL_NAME_KEY = "fullName";
    private static final String PHONE_KEY = "phone";
    private static final String PASSWORD_KEY = "password";

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final HotelRepository hotelRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * GET /api/users - Get all users (SuperAdmin only)
     */
    @GetMapping
    @org.springframework.security.access.prepost.PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userRepository.findAll().stream()
                .map(this::toUserResponse)
                .toList();
        return ResponseEntity.ok(users);
    }

    /**
     * GET /api/users/hotel/{hotelId}/technicians - Get technicians for a hotel
     * (Admin only)
     * 
     * Business rule: Technicians are linked to a hotel via their hotelId in the
     * users table.
     * This method returns only technicians for the specified hotel.
     */
    @GetMapping("/hotel/{hotelId}/technicians")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<List<UserResponse>> getTechniciansByHotel(@PathVariable UUID hotelId) {
        try {
            // Get users for the hotel
            List<User> usersInHotel = userRepository.findByHotelId(hotelId);

            // Filter to keep only active technicians
            List<UserResponse> technicians = new java.util.ArrayList<>();

            for (User user : usersInHotel) {
                if (user == null || (user.getIsActive() != null && !user.getIsActive())) {
                    continue; // Skip null or inactive users
                }

                // Check if user has TECHNICIAN role
                Optional<UserRole> userRoleOpt = userRoleRepository.findByUserId(user.getId());
                if (userRoleOpt.isPresent()) {
                    UserRole userRole = userRoleOpt.get();
                    if (userRole.getRole() == com.hotel.tickethub.model.enums.UserRole.TECHNICIAN) {
                        technicians.add(toUserResponse(user));
                    }
                }
            }

            return ResponseEntity.ok(technicians);
        } catch (Exception e) {
            // Return empty list instead of 500 error
            return ResponseEntity.status(HttpStatus.OK).body(List.of());
        }
    }

    /**
     * GET /api/users/{id} - Get user by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id) {
        return userRepository.findById(id)
                .map(user -> ResponseEntity.ok(toUserResponse(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/users - Create a new user
     */
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@RequestBody UserRequest request) {
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setIsActive(true);

        User savedUser = userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(toUserResponse(savedUser));
    }

    /**
     * PUT /api/users/{id} - Update a user
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(
            @PathVariable UUID id,
            @RequestBody UserRequest request) {
        return userRepository.findById(id).map(user -> {
            user.setEmail(request.getEmail());
            user.setFullName(request.getFullName());
            user.setPhone(request.getPhone());
            User updatedUser = userRepository.save(user);
            return ResponseEntity.ok(toUserResponse(updatedUser));
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * DELETE /api/users/{id} - Delete a user
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * PUT /api/users/technicians/{id} - Update a technician (Admin only)
     */
    @PutMapping("/technicians/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Object> updateTechnician(@PathVariable UUID id, @RequestBody Map<String, String> request) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Technician not found"));

            // Verify user is a technician
            Optional<UserRole> userRoleOpt = userRoleRepository.findByUserId(id);
            if (userRoleOpt.isEmpty()
                    || userRoleOpt.get().getRole() != com.hotel.tickethub.model.enums.UserRole.TECHNICIAN) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("User is not a technician");
            }

            // Update provided fields
            if (request.containsKey(EMAIL_KEY)) {
                String newEmail = request.get(EMAIL_KEY);
                // Check email doesn't already exist (except for this user)
                if (!user.getEmail().equals(newEmail) && userRepository.existsByEmail(newEmail)) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("A user with this email already exists");
                }
                user.setEmail(newEmail);
            }

            if (request.containsKey(FULL_NAME_KEY)) {
                user.setFullName(request.get(FULL_NAME_KEY));
            }

            if (request.containsKey(PHONE_KEY)) {
                user.setPhone(request.get(PHONE_KEY));
            }

            if (request.containsKey("isActive")) {
                user.setIsActive(Boolean.parseBoolean(request.get("isActive")));
            }

            if (request.containsKey(PASSWORD_KEY) && !request.get(PASSWORD_KEY).isEmpty()) {
                user.setPassword(passwordEncoder.encode(request.get(PASSWORD_KEY)));
            }

            user = userRepository.save(user);

            return ResponseEntity.ok(toUserResponse(user));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating technician: " + e.getMessage());
        }
    }

    /**
     * DELETE /api/users/technicians/{id} - Delete a technician (Admin only)
     */
    @DeleteMapping("/technicians/{id}")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Object> deleteTechnician(@PathVariable UUID id) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Technician not found"));

            // Verify user is a technician
            Optional<UserRole> userRoleOpt = userRoleRepository.findByUserId(id);
            if (userRoleOpt.isEmpty()
                    || userRoleOpt.get().getRole() != com.hotel.tickethub.model.enums.UserRole.TECHNICIAN) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("User is not a technician");
            }

            // Delete role first
            userRoleRepository.delete(userRoleOpt.get());

            // Delete user
            userRepository.delete(user);

            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error deleting technician: " + e.getMessage());
        }
    }

    /**
     * POST /api/users/technicians - Create a new technician (Admin only)
     * 
     * Creates a user with TECHNICIAN role and links it to the specified hotel.
     * Body: { email, password, fullName, phone, hotelId }
     */
    @PostMapping("/technicians")
    @org.springframework.security.access.prepost.PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<Object> createTechnician(@RequestBody Map<String, String> request) {
        try {
            String email = request.get(EMAIL_KEY);
            String password = request.get(PASSWORD_KEY);
            String fullName = request.get(FULL_NAME_KEY);
            String phone = request.get(PHONE_KEY);
            String hotelIdStr = request.get("hotelId");

            if (email == null || fullName == null || hotelIdStr == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Email, fullName and hotelId are required");
            }

            // Check email doesn't already exist
            if (userRepository.existsByEmail(email)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("A user with this email already exists");
            }

            // Get hotel
            UUID hotelId = UUID.fromString(hotelIdStr);
            Hotel hotel = hotelRepository.findById(hotelId)
                    .orElseThrow(() -> new RuntimeException("Hotel not found"));

            // Create user
            User user = new User();
            user.setEmail(email);
            String rawPassword = password != null && !password.isEmpty() ? password : "Technician123!";
            user.setPassword(passwordEncoder.encode(rawPassword));
            user.setFullName(fullName);
            user.setPhone(phone);
            user.setIsActive(true);
            user.setHotel(hotel);

            user = userRepository.save(user);

            // Create TECHNICIAN role
            UserRole userRole = new UserRole();
            userRole.setUser(user);
            userRole.setRole(com.hotel.tickethub.model.enums.UserRole.TECHNICIAN);
            userRole.setHotel(hotel);
            userRoleRepository.save(userRole);

            return ResponseEntity.status(HttpStatus.CREATED).body(toUserResponse(user));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating technician: " + e.getMessage());
        }
    }

    private UserResponse toUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .isActive(user.getIsActive())
                .specialties(user.getSpecialties() != null ? user.getSpecialties() : List.of())
                .build();
    }
}
