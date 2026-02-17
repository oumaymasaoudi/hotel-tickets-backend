package com.hotel.tickethub.service;

import com.hotel.tickethub.model.*;
import com.hotel.tickethub.repository.*;
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
class GdprServiceTest {

    @Mock
    private GdprConsentRepository consentRepository;

    @Mock
    private DataDeletionRequestRepository deletionRequestRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TicketRepository ticketRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private AuditLogService auditLogService;

    @InjectMocks
    private GdprService gdprService;

    private User user;
    private Hotel hotel;
    private GdprConsent consent;
    private UUID userId;
    private UUID hotelId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        hotelId = UUID.randomUUID();

        hotel = new Hotel();
        hotel.setId(hotelId);
        hotel.setName("Test Hotel");

        user = new User();
        user.setId(userId);
        user.setEmail("test@example.com");
        user.setFullName("Test User");
        user.setHotel(hotel);
        user.setCreatedAt(LocalDateTime.now());

        consent = GdprConsent.builder()
                .id(UUID.randomUUID())
                .user(user)
                .consentType("MARKETING")
                .consented(true)
                .consentDate(LocalDateTime.now())
                .privacyPolicyVersion("1.0")
                .build();
    }

    @Test
    void testRecordConsent_NewConsent() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(consentRepository.findByUserIdAndConsentType(userId, "MARKETING"))
                .thenReturn(Optional.empty());
        when(consentRepository.save(any(GdprConsent.class))).thenReturn(consent);

        // When
        GdprConsent result = gdprService.recordConsent(userId, "MARKETING", true, "127.0.0.1", "Mozilla");

        // Then
        assertNotNull(result);
        verify(consentRepository, times(1)).save(any(GdprConsent.class));
        verify(auditLogService, times(1)).logAction(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void testRecordConsent_UpdateExisting() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(consentRepository.findByUserIdAndConsentType(userId, "MARKETING"))
                .thenReturn(Optional.of(consent));
        when(consentRepository.save(any(GdprConsent.class))).thenReturn(consent);

        // When
        GdprConsent result = gdprService.recordConsent(userId, "MARKETING", false, "127.0.0.1", "Mozilla");

        // Then
        assertNotNull(result);
        verify(consentRepository, times(1)).save(any(GdprConsent.class));
    }

    @Test
    void testRecordConsent_UserNotFound() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            gdprService.recordConsent(userId, "MARKETING", true, "127.0.0.1", "Mozilla");
        });
    }

    @Test
    void testHasConsent_True() {
        // Given
        when(consentRepository.findByUserIdAndConsentType(userId, "MARKETING"))
                .thenReturn(Optional.of(consent));

        // When
        Boolean result = gdprService.hasConsent(userId, "MARKETING");

        // Then
        assertTrue(result);
    }

    @Test
    void testHasConsent_False() {
        // Given
        when(consentRepository.findByUserIdAndConsentType(userId, "MARKETING"))
                .thenReturn(Optional.empty());

        // When
        Boolean result = gdprService.hasConsent(userId, "MARKETING");

        // Then
        assertFalse(result);
    }

    @Test
    void testGetUserConsents() {
        // Given
        List<GdprConsent> consents = Arrays.asList(consent);
        when(consentRepository.findByUserId(userId)).thenReturn(consents);

        // When
        List<GdprConsent> result = gdprService.getUserConsents(userId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(consentRepository, times(1)).findByUserId(userId);
    }

    @Test
    void testExportUserData_Success() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(ticketRepository.findByClientEmail(user.getEmail())).thenReturn(Collections.emptyList());
        when(paymentRepository.findByHotelIdOrderByPaymentDateDesc(hotelId))
                .thenReturn(Collections.emptyList());
        when(consentRepository.findByUserId(userId)).thenReturn(Collections.emptyList());
        when(auditLogRepository.findByUserIdOrderByTimestampDesc(userId))
                .thenReturn(Collections.emptyList());

        // When
        Map<String, Object> result = gdprService.exportUserData(userId);

        // Then
        assertNotNull(result);
        assertTrue(result.containsKey("profile"));
        assertTrue(result.containsKey("tickets"));
        assertTrue(result.containsKey("payments"));
        assertTrue(result.containsKey("gdprConsents"));
        assertTrue(result.containsKey("auditLogs"));
        verify(auditLogService, times(1)).logAction(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void testExportUserData_UserNotFound() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            gdprService.exportUserData(userId);
        });
    }

    @Test
    void testRequestDataDeletion_Success() {
        // Given
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(deletionRequestRepository.findByUserId(userId)).thenReturn(Collections.emptyList());
        when(deletionRequestRepository.save(any(DataDeletionRequest.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        DataDeletionRequest result = gdprService.requestDataDeletion(userId, "127.0.0.1");

        // Then
        assertNotNull(result);
        assertEquals(DataDeletionRequest.DeletionStatus.PENDING, result.getStatus());
        verify(deletionRequestRepository, times(1)).save(any(DataDeletionRequest.class));
    }

    @Test
    void testRequestDataDeletion_PendingRequestExists() {
        // Given
        DataDeletionRequest existingRequest = DataDeletionRequest.builder()
                .id(UUID.randomUUID())
                .user(user)
                .status(DataDeletionRequest.DeletionStatus.PENDING)
                .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(deletionRequestRepository.findByUserId(userId))
                .thenReturn(Arrays.asList(existingRequest));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            gdprService.requestDataDeletion(userId, "127.0.0.1");
        });
    }

    @Test
    void testGetAllDeletionRequests() {
        // Given
        DataDeletionRequest request = DataDeletionRequest.builder()
                .id(UUID.randomUUID())
                .user(user)
                .status(DataDeletionRequest.DeletionStatus.PENDING)
                .build();
        List<DataDeletionRequest> requests = Arrays.asList(request);
        when(deletionRequestRepository.findAll()).thenReturn(requests);

        // When
        List<DataDeletionRequest> result = gdprService.getAllDeletionRequests();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(deletionRequestRepository, times(1)).findAll();
    }

    @Test
    void testGetPendingDeletionRequests() {
        // Given
        DataDeletionRequest request = DataDeletionRequest.builder()
                .id(UUID.randomUUID())
                .user(user)
                .status(DataDeletionRequest.DeletionStatus.PENDING)
                .build();
        List<DataDeletionRequest> requests = Arrays.asList(request);
        when(deletionRequestRepository.findByStatus(DataDeletionRequest.DeletionStatus.PENDING))
                .thenReturn(requests);

        // When
        List<DataDeletionRequest> result = gdprService.getPendingDeletionRequests();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(deletionRequestRepository, times(1))
                .findByStatus(DataDeletionRequest.DeletionStatus.PENDING);
    }
}
