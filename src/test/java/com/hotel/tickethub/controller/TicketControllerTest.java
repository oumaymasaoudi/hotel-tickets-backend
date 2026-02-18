package com.hotel.tickethub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotel.tickethub.dto.CreateTicketRequest;
import com.hotel.tickethub.dto.TicketResponse;
import com.hotel.tickethub.service.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class TicketControllerTest {

    @MockBean
    private TicketService ticketService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private TicketController ticketController;

    private TicketResponse ticketResponse;
    private CreateTicketRequest createRequest;

    @BeforeEach
    void setUp() {
        ticketResponse = new TicketResponse();
        ticketResponse.setId(UUID.randomUUID());
        ticketResponse.setTicketNumber("TKT-001");
        ticketResponse.setDescription("Test ticket");

        createRequest = new CreateTicketRequest();
        createRequest.setHotelId(UUID.randomUUID());
        createRequest.setCategoryId(UUID.randomUUID());
        createRequest.setDescription("Test description");
        createRequest.setClientEmail("test@example.com");
        createRequest.setIsUrgent(false);
    }

    @Test
    void testCreateTicket_Success() throws Exception {
        // Given
        when(ticketService.createTicket(any(CreateTicketRequest.class), anyList()))
                .thenReturn(ticketResponse);

        String ticketJson = objectMapper.writeValueAsString(createRequest);
        MockMultipartFile ticketPart = new MockMultipartFile(
                "ticket", 
                "ticket.json", 
                MediaType.APPLICATION_JSON_VALUE, 
                ticketJson.getBytes()
        );

        // When & Then
        // Le contrôleur retourne 201 CREATED avec body JSON
        mockMvc.perform(multipart("/api/tickets/public")
                        .file(ticketPart)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.ticketNumber").value("TKT-001"));

        verify(ticketService, times(1)).createTicket(any(CreateTicketRequest.class), anyList());
    }

    @Test
    void testCreateTicket_WithImages() throws Exception {
        // Given
        when(ticketService.createTicket(any(CreateTicketRequest.class), anyList()))
                .thenReturn(ticketResponse);

        String ticketJson = objectMapper.writeValueAsString(createRequest);
        MockMultipartFile ticketPart = new MockMultipartFile(
                "ticket", 
                "ticket.json", 
                MediaType.APPLICATION_JSON_VALUE, 
                ticketJson.getBytes()
        );
        MockMultipartFile image1 = new MockMultipartFile(
                "images", 
                "test1.jpg", 
                "image/jpeg", 
                "content1".getBytes()
        );
        MockMultipartFile image2 = new MockMultipartFile(
                "images", 
                "test2.jpg", 
                "image/jpeg", 
                "content2".getBytes()
        );

        // When & Then
        mockMvc.perform(multipart("/api/tickets/public")
                        .file(ticketPart)
                        .file(image1)
                        .file(image2))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));

        verify(ticketService, times(1)).createTicket(any(CreateTicketRequest.class), anyList());
    }

    @Test
    void testCreateTicket_Error() throws Exception {
        // Given
        when(ticketService.createTicket(any(CreateTicketRequest.class), anyList()))
                .thenThrow(new RuntimeException("Error"));

        String ticketJson = objectMapper.writeValueAsString(createRequest);
        MockMultipartFile ticketPart = new MockMultipartFile(
                "ticket", 
                "ticket.json", 
                MediaType.APPLICATION_JSON_VALUE, 
                ticketJson.getBytes()
        );

        // When & Then
        // GlobalExceptionHandler gère RuntimeException et retourne BAD_REQUEST avec body JSON
        mockMvc.perform(multipart("/api/tickets/public")
                        .file(ticketPart)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").exists());

        verify(ticketService, times(1)).createTicket(any(CreateTicketRequest.class), anyList());
    }

    @Test
    void testCreateTicket_ValidationError() throws Exception {
        // Given - CreateTicketRequest invalide (champs obligatoires manquants)
        CreateTicketRequest invalidRequest = new CreateTicketRequest();
        // hotelId et categoryId sont null (violation @NotNull)
        // clientEmail est null (violation @NotBlank @Email)
        // description est null (violation @NotBlank)
        
        String ticketJson = objectMapper.writeValueAsString(invalidRequest);
        MockMultipartFile ticketPart = new MockMultipartFile(
                "ticket", 
                "ticket.json", 
                MediaType.APPLICATION_JSON_VALUE, 
                ticketJson.getBytes()
        );

        // When & Then
        // MethodArgumentNotValidException devrait être gérée par GlobalExceptionHandler
        mockMvc.perform(multipart("/api/tickets/public")
                        .file(ticketPart)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.error").exists())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        // Le service ne devrait pas être appelé si la validation échoue
        verify(ticketService, never()).createTicket(any(CreateTicketRequest.class), anyList());
    }

    @Test
    void testGetTicketByNumber_Success() throws Exception {
        // Given
        String ticketNumber = "TKT-001";
        when(ticketService.getTicketByNumber(ticketNumber)).thenReturn(ticketResponse);

        // When & Then
        mockMvc.perform(get("/api/tickets/public/{ticketNumber}", ticketNumber))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ticketNumber").value("TKT-001"));

        verify(ticketService, times(1)).getTicketByNumber(ticketNumber);
    }

    @Test
    void testGetTicketsByEmail_Success() throws Exception {
        // Given
        String email = "test@example.com";
        List<TicketResponse> tickets = Arrays.asList(ticketResponse);
        when(ticketService.getTicketsByEmail(email)).thenReturn(tickets);

        // When & Then
        mockMvc.perform(get("/api/tickets/public/email/{email}", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].ticketNumber").value("TKT-001"));

        verify(ticketService, times(1)).getTicketsByEmail(email);
    }

    @Test
    void testGetTicketsByEmail_Empty() throws Exception {
        // Given
        String email = "empty@example.com";
        when(ticketService.getTicketsByEmail(email)).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/tickets/public/email/{email}", email))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
}
