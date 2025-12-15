package com.hotel.tickethub.service;

import com.hotel.tickethub.dto.TicketResponse;
import com.hotel.tickethub.model.Category;
import com.hotel.tickethub.model.Hotel;
import com.hotel.tickethub.model.Ticket;
import com.hotel.tickethub.model.enums.TicketStatus;
import com.hotel.tickethub.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TicketImageRepository ticketImageRepository;

    @Mock
    private TicketHistoryRepository ticketHistoryRepository;

    @Mock
    private PlanRepository planRepository;

    @InjectMocks
    private TicketService ticketService;

    private Ticket testTicket;
    private Hotel testHotel;
    private Category testCategory;
    private UUID testTicketId;

    @BeforeEach
    void setUp() {
        testTicketId = UUID.randomUUID();
        UUID hotelId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        // Créer un Hotel de test
        testHotel = new Hotel();
        testHotel.setId(hotelId);
        testHotel.setName("Test Hotel");

        // Créer une Category de test
        testCategory = new Category();
        testCategory.setId(categoryId);
        testCategory.setName("Test Category");
        testCategory.setIcon("icon-test");
        testCategory.setColor("#FF0000");

        // Créer un Ticket de test complet
        testTicket = new Ticket();
        testTicket.setId(testTicketId);
        testTicket.setTicketNumber("TKT-12345");
        testTicket.setHotel(testHotel);
        testTicket.setCategory(testCategory);
        testTicket.setClientEmail("test@example.com");
        testTicket.setClientPhone("123456789");
        testTicket.setDescription("Test description");
        testTicket.setStatus(TicketStatus.OPEN);
        testTicket.setIsUrgent(false);
    }

    @Test
    void testGetTicketById() {
        when(ticketRepository.findById(testTicketId)).thenReturn(Optional.of(testTicket));
        when(ticketImageRepository.findByTicketId(testTicketId)).thenReturn(Collections.emptyList());

        TicketResponse result = ticketService.getTicketById(testTicketId);

        assertNotNull(result);
        assertEquals(testTicketId, result.getId());
        assertEquals(testTicket.getTicketNumber(), result.getTicketNumber());
        assertEquals(testHotel.getId(), result.getHotelId());
        assertEquals(testHotel.getName(), result.getHotelName());
        assertEquals(testCategory.getId(), result.getCategoryId());
        assertEquals(testCategory.getName(), result.getCategoryName());
        verify(ticketRepository, times(1)).findById(testTicketId);
        verify(ticketImageRepository, times(1)).findByTicketId(testTicketId);
    }

    @Test
    void testGetTicketByIdNotFound() {
        when(ticketRepository.findById(testTicketId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            ticketService.getTicketById(testTicketId);
        });

        verify(ticketRepository, times(1)).findById(testTicketId);
        verify(ticketImageRepository, never()).findByTicketId(any());
    }
}
