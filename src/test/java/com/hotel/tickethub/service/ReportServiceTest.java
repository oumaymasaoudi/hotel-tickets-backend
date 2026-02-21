package com.hotel.tickethub.service;

import com.hotel.tickethub.model.Hotel;
import com.hotel.tickethub.model.Ticket;
import com.hotel.tickethub.model.User;
import com.hotel.tickethub.model.enums.TicketStatus;
import com.hotel.tickethub.repository.HotelRepository;
import com.hotel.tickethub.repository.TicketRepository;
import com.hotel.tickethub.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaymentService paymentService;

    @InjectMocks
    private ReportService reportService;

    private Hotel hotel;
    private Ticket ticket;
    private User technician;
    private UUID hotelId;

    @BeforeEach
    void setUp() {
        hotelId = UUID.randomUUID();
        UUID ticketId = UUID.randomUUID();
        UUID technicianId = UUID.randomUUID();

        hotel = new Hotel();
        hotel.setId(hotelId);
        hotel.setName("Test Hotel");
        hotel.setIsActive(true);

        technician = new User();
        technician.setId(technicianId);
        technician.setFullName("John Technician");

        ticket = new Ticket();
        ticket.setId(ticketId);
        ticket.setHotel(hotel);
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setIsUrgent(false);
        ticket.setCreatedAt(LocalDateTime.now().minusDays(1));
        ticket.setAssignedTechnician(technician);
    }

    @Test
    void testGenerateMonthlyReport_Success() {
        // Given
        int year = 2026;
        int month = 2;
        List<Ticket> tickets = Arrays.asList(ticket);
        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel));
        when(ticketRepository.findByHotelIdAndCreatedAtBetween(any(UUID.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(tickets);
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(technician));
        when(paymentService.getPaymentHistory(hotelId)).thenReturn(Collections.emptyList());

        // When
        Map<String, Object> result = reportService.generateMonthlyReport(hotelId, year, month);

        // Then
        assertNotNull(result);
        assertEquals("Mensuel", result.get("type"));
        assertEquals(hotelId, result.get("hotelId"));
        assertEquals("Test Hotel", result.get("hotelName"));
        assertTrue(result.containsKey("totalTickets"));
        verify(hotelRepository, times(1)).findById(hotelId);
    }

    @Test
    void testGenerateMonthlyReport_HotelNotFound() {
        // Given
        when(hotelRepository.findById(hotelId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            reportService.generateMonthlyReport(hotelId, 2026, 2);
        });
    }

    @Test
    void testGenerateWeeklyReport_Success() {
        // Given
        LocalDateTime weekStart = LocalDateTime.now().minusDays(7);
        List<Ticket> tickets = Arrays.asList(ticket);
        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel));
        when(ticketRepository.findByHotelIdAndCreatedAtBetween(any(UUID.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(tickets);
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(technician));
        when(paymentService.getPaymentHistory(hotelId)).thenReturn(Collections.emptyList());

        // When
        Map<String, Object> result = reportService.generateWeeklyReport(hotelId, weekStart);

        // Then
        assertNotNull(result);
        assertEquals("Hebdomadaire", result.get("type"));
        verify(hotelRepository, times(1)).findById(hotelId);
    }

    @Test
    void testGenerateDailyReport_Success() {
        // Given
        LocalDateTime date = LocalDateTime.now();
        List<Ticket> tickets = Arrays.asList(ticket);
        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel));
        when(ticketRepository.findByHotelIdAndCreatedAtBetween(any(UUID.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(tickets);
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(technician));
        when(paymentService.getPaymentHistory(hotelId)).thenReturn(Collections.emptyList());

        // When
        Map<String, Object> result = reportService.generateDailyReport(hotelId, date);

        // Then
        assertNotNull(result);
        assertEquals("Quotidien", result.get("type"));
        verify(hotelRepository, times(1)).findById(hotelId);
    }

    @Test
    void testGenerateGlobalReport_Success() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusMonths(1);
        LocalDateTime endDate = LocalDateTime.now();
        List<Hotel> hotels = Arrays.asList(hotel);
        List<Ticket> tickets = Arrays.asList(ticket);

        when(hotelRepository.findAll()).thenReturn(hotels);
        when(ticketRepository.findByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(tickets);
        when(ticketRepository.findByHotelIdAndCreatedAtBetween(any(UUID.class), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(tickets);
        when(userRepository.findById(any(UUID.class))).thenReturn(Optional.of(technician));
        when(paymentService.getOverduePayments()).thenReturn(Collections.emptyList());

        // When
        Map<String, Object> result = reportService.generateGlobalReport(startDate, endDate);

        // Then
        assertNotNull(result);
        assertEquals("Global", result.get("type"));
        assertTrue(result.containsKey("totalTickets"));
        assertTrue(result.containsKey("hotels"));
        assertTrue(result.containsKey("technicians"));
        verify(hotelRepository, times(1)).findAll();
    }

    @Test
    void testGenerateGlobalReport_EmptyData() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusMonths(1);
        LocalDateTime endDate = LocalDateTime.now();

        when(hotelRepository.findAll()).thenReturn(Collections.emptyList());
        when(ticketRepository.findByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());
        when(paymentService.getOverduePayments()).thenReturn(Collections.emptyList());

        // When
        Map<String, Object> result = reportService.generateGlobalReport(startDate, endDate);

        // Then
        assertNotNull(result);
        assertEquals(0, result.get("totalTickets"));
        assertEquals(0, result.get("totalHotels"));
    }
}
