package com.hotel.tickethub.controller;

import com.hotel.tickethub.model.User;
import com.hotel.tickethub.model.UserRole;
import com.hotel.tickethub.repository.UserRepository;
import com.hotel.tickethub.repository.UserRoleRepository;
import com.hotel.tickethub.repository.HotelRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.hotel.tickethub.service.PaymentService;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class UserRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private UserRoleRepository userRoleRepository;

    @MockBean
    private HotelRepository hotelRepository;

    @MockBean
    private PaymentService paymentService;

    private User testUser;
    private UserRole testUserRole;
    private UUID testUserId;
    private UUID testHotelId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testHotelId = UUID.randomUUID();

        testUser = new User();
        testUser.setId(testUserId);
        testUser.setEmail("test@example.com");
        testUser.setFullName("Test User");
        testUser.setIsActive(true);

        testUserRole = new UserRole();
        testUserRole.setUser(testUser);
        testUserRole.setRole(com.hotel.tickethub.model.enums.UserRole.TECHNICIAN);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetTechniciansByHotel() throws Exception {
        when(userRepository.findByHotelId(any(UUID.class))).thenReturn(java.util.List.of(testUser));
        when(userRoleRepository.findByUserId(any(UUID.class))).thenReturn(Optional.of(testUserRole));

        mockMvc.perform(get("/api/users/hotel/{hotelId}/technicians", testHotelId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateTechnician() throws Exception {
        when(hotelRepository.findById(any(UUID.class))).thenReturn(Optional.of(new com.hotel.tickethub.model.Hotel()));
        when(userRepository.existsByEmail(any(String.class))).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(userRoleRepository.save(any(UserRole.class))).thenReturn(testUserRole);

        String requestBody = """
                {
                    "email": "technician@example.com",
                    "fullName": "Test Technician",
                    "phone": "+33612345678",
                    "password": "Test123!",
                    "hotelId": "%s"
                }
                """.formatted(testHotelId);

        mockMvc.perform(post("/api/users/technicians")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated());
    }
}
