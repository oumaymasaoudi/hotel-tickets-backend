package com.hotel.tickethub.controller;

import com.hotel.tickethub.dto.CreateTicketRequest;
import com.hotel.tickethub.dto.TicketResponse;
import com.hotel.tickethub.dto.UpdateTicketStatusRequest;
import com.hotel.tickethub.model.enums.TicketStatus;
import com.hotel.tickethub.service.TicketService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketControllerTest {

    @Mock
    private TicketService ticketService;

    @InjectMocks
    private TicketController ticketController;

    private TicketResponse ticketResponse;
    private CreateTicketRequest createRequest;
    private UpdateTicketStatusRequest updateRequest;

    @BeforeEach
    void setUp() {
        ticketResponse = new TicketResponse();
        ticketResponse.setId(UUID.randomUUID());
        ticketResponse.setTicketNumber("TKT-001");
        ticketResponse.setDescription("Test ticket");

        createRequest = new CreateTicketRequest();
        createRequest.setDescription("Test description");
        createRequest.setClientEmail("test@example.com");

        updateRequest = new UpdateTicketStatusRequest();
        updateRequest.setStatus(TicketStatus.RESOLVED);
    }

    @Test
    void testCreateTicket_Success() {
        // Given
        when(ticketService.createTicket(any(CreateTicketRequest.class), anyList()))
                .thenReturn(ticketResponse);

        // When
        ResponseEntity<TicketResponse> response = ticketController.createTicket(createRequest, null);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("TKT-001", response.getBody().getTicketNumber());
        verify(ticketService, times(1)).createTicket(any(), anyList());
    }

    @Test
    void testCreateTicket_WithImages() {
        // Given
        List<MultipartFile> images = Arrays.asList(
                new MockMultipartFile("image1", "test1.jpg", "image/jpeg", "content1".getBytes()),
                new MockMultipartFile("image2", "test2.jpg", "image/jpeg", "content2".getBytes())
        );
        when(ticketService.createTicket(any(CreateTicketRequest.class), anyList()))
                .thenReturn(ticketResponse);

        // When
        ResponseEntity<TicketResponse> response = ticketController.createTicket(createRequest, images);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(ticketService, times(1)).createTicket(any(), anyList());
    }

    @Test
    void testCreateTicket_Error() {
        // Given
        when(ticketService.createTicket(any(CreateTicketRequest.class), anyList()))
                .thenThrow(new RuntimeException("Error"));

        // When
        ResponseEntity<TicketResponse> response = ticketController.createTicket(createRequest, null);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void testGetTicketByNumber_Success() {
        // Given
        String ticketNumber = "TKT-001";
        when(ticketService.getTicketByNumber(ticketNumber)).thenReturn(ticketResponse);

        // When
        ResponseEntity<TicketResponse> response = ticketController.getTicketByNumber(ticketNumber);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(ticketService, times(1)).getTicketByNumber(ticketNumber);
    }

    @Test
    void testGetTicketsByEmail_Success() {
        // Given
        String email = "test@example.com";
        List<TicketResponse> tickets = Arrays.asList(ticketResponse);
        when(ticketService.getTicketsByEmail(email)).thenReturn(tickets);

        // When
        ResponseEntity<List<TicketResponse>> response = ticketController.getTicketsByEmail(email);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        verify(ticketService, times(1)).getTicketsByEmail(email);
    }

    @Test
    void testGetTicketsByEmail_Empty() {
        // Given
        String email = "empty@example.com";
        when(ticketService.getTicketsByEmail(email)).thenReturn(Collections.emptyList());

        // When
        ResponseEntity<List<TicketResponse>> response = ticketController.getTicketsByEmail(email);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }
}
