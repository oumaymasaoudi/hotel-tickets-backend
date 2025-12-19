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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private HotelRepository hotelRepository;

    @InjectMocks
    private AuthService authService;

    private Hotel testHotel;
    private User testUser;
    private UserRole testUserRole;
    private UUID testUserId;
    private UUID testHotelId;

    @BeforeEach
    void setUp() {
        testHotelId = UUID.randomUUID();
        testUserId = UUID.randomUUID();

        testHotel = new Hotel();
        testHotel.setId(testHotelId);
        testHotel.setName("Test Hotel");

        testUser = new User();
        testUser.setId(testUserId);
        testUser.setEmail("test@example.com");
        testUser.setPassword("password123");
        testUser.setFullName("Test User");
        testUser.setIsActive(true);
        testUser.setHotel(testHotel);

        testUserRole = new UserRole();
        testUserRole.setId(UUID.randomUUID());
        testUserRole.setUser(testUser);
        testUserRole.setRole(com.hotel.tickethub.model.enums.UserRole.CLIENT);
        testUserRole.setHotel(testHotel);
    }

    @Test
    void testRegister_Success() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("newuser@example.com");
        request.setPassword("password123");
        request.setFullName("New User");
        request.setPhone("1234567890");
        request.setRole("CLIENT");

        when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });
        when(userRoleRepository.save(any(UserRole.class))).thenAnswer(invocation -> {
            UserRole role = invocation.getArgument(0);
            role.setId(UUID.randomUUID());
            return role;
        });

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("newuser@example.com", response.getEmail());
        assertEquals("New User", response.getFullName());
        assertEquals("CLIENT", response.getRole());
        verify(userRepository, times(1)).save(any(User.class));
        verify(userRoleRepository, times(1)).save(any(UserRole.class));
    }

    @Test
    void testRegister_EmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@example.com");
        request.setPassword("password123");
        request.setFullName("Existing User");
        request.setRole("CLIENT");

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.register(request);
        });

        assertEquals("Email already exists", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testRegister_AdminWithoutHotel() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("admin@example.com");
        request.setPassword("password123");
        request.setFullName("Admin User");
        request.setRole("ADMIN");
        request.setHotelId(null);

        when(userRepository.existsByEmail("admin@example.com")).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.register(request);
        });

        assertTrue(exception.getMessage().contains("must be linked to a hotel"));
    }

    @Test
    void testRegister_SuperAdminBlocked() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("superadmin@example.com");
        request.setPassword("password123");
        request.setFullName("Super Admin");
        request.setRole("SUPERADMIN");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.register(request);
        });

        assertTrue(exception.getMessage().contains("SuperAdmin role cannot be created"));
        verify(userRepository, never()).existsByEmail(anyString());
    }

    @Test
    void testLogin_Success() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userRoleRepository.findByUserId(testUserId)).thenReturn(Optional.of(testUserRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("test@example.com", response.getEmail());
        assertEquals("Test User", response.getFullName());
        assertEquals("CLIENT", response.getRole());
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void testLogin_UserNotFound() {
        LoginRequest request = new LoginRequest();
        request.setEmail("notfound@example.com");
        request.setPassword("password123");

        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(request);
        });

        assertTrue(exception.getMessage().contains("User not found"));
    }

    @Test
    void testLogin_WrongPassword() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("wrongpassword");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userRoleRepository.findByUserId(testUserId)).thenReturn(Optional.of(testUserRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(request);
        });

        assertTrue(exception.getMessage().contains("Incorrect password"));
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void testLogin_AccountLocked() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        testUser.setLockedUntil(LocalDateTime.now().plusMinutes(15));
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(request);
        });

        assertTrue(exception.getMessage().contains("Account locked"));
    }

    @Test
    void testLogin_MultipleFailedAttempts() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@example.com");
        request.setPassword("wrongpassword");

        testUser.setFailedLoginAttempts(4);
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userRoleRepository.findByUserId(testUserId)).thenReturn(Optional.of(testUserRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.login(request);
        });

        assertTrue(exception.getMessage().contains("Account locked after 5 failed attempts"));
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void testUpdateUserRole_Success() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(userRoleRepository.findByUserId(testUserId)).thenReturn(Optional.of(testUserRole));
        when(userRoleRepository.save(any(UserRole.class))).thenReturn(testUserRole);

        assertDoesNotThrow(() -> {
            authService.updateUserRole("test@example.com", "ADMIN");
        });

        verify(userRoleRepository, times(1)).save(any(UserRole.class));
    }

    @Test
    void testUpdateUserRole_SuperAdminBlocked() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.updateUserRole("test@example.com", "SUPERADMIN");
        });

        assertTrue(exception.getMessage().contains("SuperAdmin role cannot be assigned"));
    }

    @Test
    void testCreateSuperAdmin_Success() {
        when(userRoleRepository.findAll()).thenReturn(java.util.Collections.emptyList());
        when(userRepository.existsByEmail("superadmin@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });
        when(userRoleRepository.save(any(UserRole.class))).thenAnswer(invocation -> {
            UserRole role = invocation.getArgument(0);
            role.setId(UUID.randomUUID());
            return role;
        });

        AuthResponse response = authService.createSuperAdmin(
                "superadmin@example.com",
                "password123",
                "Super Admin");

        assertNotNull(response);
        assertEquals("superadmin@example.com", response.getEmail());
        assertEquals("SUPERADMIN", response.getRole());
        verify(userRepository, times(1)).save(any(User.class));
        verify(userRoleRepository, times(1)).save(any(UserRole.class));
    }

    @Test
    void testCreateSuperAdmin_AlreadyExists() {
        UserRole existingSuperAdmin = new UserRole();
        existingSuperAdmin.setRole(com.hotel.tickethub.model.enums.UserRole.SUPERADMIN);
        when(userRoleRepository.findAll()).thenReturn(java.util.Collections.singletonList(existingSuperAdmin));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            authService.createSuperAdmin("superadmin@example.com", "password123", "Super Admin");
        });

        assertTrue(exception.getMessage().contains("A SuperAdmin already exists"));
    }
}
