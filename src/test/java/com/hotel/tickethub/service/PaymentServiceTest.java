package com.hotel.tickethub.service;

import com.hotel.tickethub.model.Hotel;
import com.hotel.tickethub.model.Payment;
import com.hotel.tickethub.model.enums.PaymentStatus;
import com.hotel.tickethub.repository.HotelRepository;
import com.hotel.tickethub.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private HotelRepository hotelRepository;

    @InjectMocks
    private PaymentService paymentService;

    private Hotel hotel;
    private Payment payment;
    private UUID hotelId;
    private UUID paymentId;

    @BeforeEach
    void setUp() {
        hotelId = UUID.randomUUID();
        paymentId = UUID.randomUUID();

        hotel = new Hotel();
        hotel.setId(hotelId);
        hotel.setName("Test Hotel");
        hotel.setIsActive(true);

        payment = new Payment();
        payment.setId(paymentId);
        payment.setAmount(BigDecimal.valueOf(100.0));
        payment.setStatus(PaymentStatus.PAID);
        payment.setPaymentDate(LocalDateTime.now());
        payment.setNextPaymentDate(LocalDateTime.now().plusMonths(1));
        payment.setHotel(hotel);
    }

    @Test
    void testCreatePayment_Success() {
        // Given
        when(hotelRepository.findById(hotelId)).thenReturn(Optional.of(hotel));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        when(hotelRepository.save(any(Hotel.class))).thenReturn(hotel);

        // When
        Payment result = paymentService.createPayment(hotelId, payment);

        // Then
        assertNotNull(result);
        assertEquals(PaymentStatus.PAID, result.getStatus());
        assertNotNull(result.getPaymentDate());
        verify(hotelRepository, times(1)).findById(hotelId);
        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(hotelRepository, times(1)).save(any(Hotel.class));
    }

    @Test
    void testCreatePayment_HotelNotFound() {
        // Given
        when(hotelRepository.findById(hotelId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            paymentService.createPayment(hotelId, payment);
        });
        verify(hotelRepository, times(1)).findById(hotelId);
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void testGetPaymentHistory_Success() {
        // Given
        List<Payment> payments = Arrays.asList(payment);
        when(paymentRepository.findByHotelIdOrderByPaymentDateDesc(hotelId)).thenReturn(payments);

        // When
        List<Payment> result = paymentService.getPaymentHistory(hotelId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(paymentRepository, times(1)).findByHotelIdOrderByPaymentDateDesc(hotelId);
    }

    @Test
    void testGetLastPayment_Success() {
        // Given
        when(paymentRepository.findFirstByHotelIdOrderByPaymentDateDesc(hotelId))
                .thenReturn(Optional.of(payment));

        // When
        Optional<Payment> result = paymentService.getLastPayment(hotelId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(paymentId, result.get().getId());
        verify(paymentRepository, times(1)).findFirstByHotelIdOrderByPaymentDateDesc(hotelId);
    }

    @Test
    void testGetLastPayment_NotFound() {
        // Given
        when(paymentRepository.findFirstByHotelIdOrderByPaymentDateDesc(hotelId))
                .thenReturn(Optional.empty());

        // When
        Optional<Payment> result = paymentService.getLastPayment(hotelId);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void testIsPaymentUpToDate_True() {
        // Given
        payment.setNextPaymentDate(LocalDateTime.now().plusDays(10));
        payment.setStatus(PaymentStatus.PAID);
        when(paymentRepository.findFirstByHotelIdOrderByPaymentDateDesc(hotelId))
                .thenReturn(Optional.of(payment));

        // When
        Boolean result = paymentService.isPaymentUpToDate(hotelId);

        // Then
        assertTrue(result);
    }

    @Test
    void testIsPaymentUpToDate_False_NoPayment() {
        // Given
        when(paymentRepository.findFirstByHotelIdOrderByPaymentDateDesc(hotelId))
                .thenReturn(Optional.empty());

        // When
        Boolean result = paymentService.isPaymentUpToDate(hotelId);

        // Then
        assertFalse(result);
    }

    @Test
    void testIsPaymentUpToDate_False_Overdue() {
        // Given
        payment.setNextPaymentDate(LocalDateTime.now().minusDays(1));
        when(paymentRepository.findFirstByHotelIdOrderByPaymentDateDesc(hotelId))
                .thenReturn(Optional.of(payment));

        // When
        Boolean result = paymentService.isPaymentUpToDate(hotelId);

        // Then
        assertFalse(result);
    }

    @Test
    void testGetOverduePayments() {
        // Given
        List<Payment> overduePayments = Arrays.asList(payment);
        when(paymentRepository.findOverduePayments()).thenReturn(overduePayments);

        // When
        List<Payment> result = paymentService.getOverduePayments();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(paymentRepository, times(1)).findOverduePayments();
    }

    @Test
    void testGetAllPayments() {
        // Given
        Payment payment2 = new Payment();
        payment2.setPaymentDate(LocalDateTime.now().minusDays(1));
        List<Payment> allPayments = Arrays.asList(payment, payment2);
        when(paymentRepository.findAll()).thenReturn(allPayments);

        // When
        List<Payment> result = paymentService.getAllPayments();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(paymentRepository, times(1)).findAll();
    }

    @Test
    void testGetPaymentsByPeriod() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusMonths(1);
        LocalDateTime endDate = LocalDateTime.now();
        List<Payment> payments = Arrays.asList(payment);
        when(paymentRepository.findPaymentsByPeriod(hotelId, startDate, endDate))
                .thenReturn(payments);

        // When
        List<Payment> result = paymentService.getPaymentsByPeriod(hotelId, startDate, endDate);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(paymentRepository, times(1)).findPaymentsByPeriod(hotelId, startDate, endDate);
    }

    @Test
    void testGetTotalPaymentAmount() {
        // Given
        LocalDateTime startDate = LocalDateTime.now().minusMonths(1);
        LocalDateTime endDate = LocalDateTime.now();
        Payment payment2 = new Payment();
        payment2.setAmount(BigDecimal.valueOf(50.0));
        List<Payment> payments = Arrays.asList(payment, payment2);
        when(paymentRepository.findPaymentsByPeriod(hotelId, startDate, endDate))
                .thenReturn(payments);

        // When
        double result = paymentService.getTotalPaymentAmount(hotelId, startDate, endDate);

        // Then
        assertEquals(150.0, result);
    }

    @Test
    void testUpdatePaymentStatus_Success() {
        // Given
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        // When
        Payment result = paymentService.updatePaymentStatus(paymentId, PaymentStatus.OVERDUE);

        // Then
        assertNotNull(result);
        assertEquals(PaymentStatus.OVERDUE, result.getStatus());
        verify(paymentRepository, times(1)).findById(paymentId);
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }

    @Test
    void testUpdatePaymentStatus_PaymentNotFound() {
        // Given
        when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            paymentService.updatePaymentStatus(paymentId, PaymentStatus.OVERDUE);
        });
    }

    @Test
    void testCheckAndMarkOverduePayments() {
        // Given
        payment.setStatus(PaymentStatus.PAID);
        List<Payment> overduePayments = Arrays.asList(payment);
        when(paymentRepository.findOverduePayments()).thenReturn(overduePayments);
        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        // When
        paymentService.checkAndMarkOverduePayments();

        // Then
        verify(paymentRepository, times(1)).findOverduePayments();
        verify(paymentRepository, times(1)).save(any(Payment.class));
    }
}
