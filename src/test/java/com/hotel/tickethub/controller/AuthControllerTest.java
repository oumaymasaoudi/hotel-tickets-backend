package com.hotel.tickethub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotel.tickethub.dto.AuthResponse;
import com.hotel.tickethub.dto.LoginRequest;
import com.hotel.tickethub.dto.RegisterRequest;
import com.hotel.tickethub.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private AuthResponse authResponse;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setEmail("test@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setFullName("Test User");
        registerRequest.setPhone("1234567890");
        registerRequest.setRole("CLIENT");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");

        authResponse = AuthResponse.builder()
                .token("dev-token")
                .email("test@example.com")
                .userId("user-id")
                .fullName("Test User")
                .role("CLIENT")
                .build();
    }

    @Test
    void testRegister_Success() throws Exception {
        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.fullName").value("Test User"))
                .andExpect(jsonPath("$.role").value("CLIENT"));
    }

    @Test
    void testRegister_Error() throws Exception {
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new RuntimeException("Email already exists"));

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email already exists"));
    }

    @Test
    void testLogin_Success() throws Exception {
        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.token").value("dev-token"))
                .andExpect(jsonPath("$.role").value("CLIENT"));
    }

    @Test
    void testLogin_Error() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("User not found"))
                .andExpect(jsonPath("$.error").value("Login failed"));
    }

    @Test
    void testFixUserRole_Success() throws Exception {
        mockMvc.perform(post("/api/auth/fix-role")
                .param("email", "test@example.com")
                .param("role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(content().string("Role updated successfully for test@example.com"));
    }

    @Test
    void testCreateSuperAdmin_Success() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("email", "superadmin@example.com");
        request.put("password", "password123");
        request.put("fullName", "Super Admin");

        AuthResponse superAdminResponse = AuthResponse.builder()
                .token("dev-token")
                .email("superadmin@example.com")
                .userId("superadmin-id")
                .fullName("Super Admin")
                .role("SUPERADMIN")
                .build();

        when(authService.createSuperAdmin(anyString(), anyString(), anyString()))
                .thenReturn(superAdminResponse);

        mockMvc.perform(post("/api/auth/create-superadmin")
                .param("secret", "DEV_SECRET_CHANGE_IN_PRODUCTION")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("superadmin@example.com"))
                .andExpect(jsonPath("$.role").value("SUPERADMIN"));
    }

    @Test
    void testCreateSuperAdmin_WrongSecret() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("email", "superadmin@example.com");
        request.put("password", "password123");
        request.put("fullName", "Super Admin");

        mockMvc.perform(post("/api/auth/create-superadmin")
                .param("secret", "wrong-secret")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Access denied. Secret key required."));
    }

    @Test
    void testCreateSuperAdmin_MissingFields() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("email", "superadmin@example.com");
        request.put("password", "password123");

        mockMvc.perform(post("/api/auth/create-superadmin")
                .param("secret", "DEV_SECRET_CHANGE_IN_PRODUCTION")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email, password and fullName are required"));
    }
}
