package com.hotel.tickethub.service;

import com.hotel.tickethub.dto.AuditLogRequest;
import com.hotel.tickethub.model.AuditLog;
import com.hotel.tickethub.model.Hotel;
import com.hotel.tickethub.model.User;
import com.hotel.tickethub.repository.AuditLogRepository;
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
class AuditLogServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private AuditLogService auditLogService;

    private User user;
    private Hotel hotel;
    private AuditLog auditLog;
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
        user.setHotel(hotel);

        auditLog = new AuditLog();
        auditLog.setId(UUID.randomUUID());
        auditLog.setUser(user);
        auditLog.setAction("TEST_ACTION");
        auditLog.setEntityType("TestEntity");
        auditLog.setTimestamp(LocalDateTime.now());
    }

    @Test
    void testLogAction_Success() {
        // Given
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("key", "value");
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(auditLog);
        AuditLogRequest request = AuditLogRequest.builder()
                .user(user)
                .action("TEST_ACTION")
                .entityType("TestEntity")
                .entityId(UUID.randomUUID())
                .hotel(hotel)
                .changes(metadata)
                .description("Test description")
                .ipAddress("127.0.0.1")
                .build();

        // When
        auditLogService.logAction(request);

        // Then
        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }

    @Test
    void testLogAction_WithNullMetadata() {
        // Given
        when(auditLogRepository.save(any(AuditLog.class))).thenReturn(auditLog);
        AuditLogRequest request = AuditLogRequest.builder()
                .user(user)
                .action("TEST_ACTION")
                .entityType("TestEntity")
                .entityId(UUID.randomUUID())
                .hotel(hotel)
                .changes(null)
                .description("Test description")
                .ipAddress(null)
                .build();

        // When
        auditLogService.logAction(request);

        // Then
        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }

    @Test
    void testGetAuditLogsByUser() {
        // Given
        List<AuditLog> logs = Arrays.asList(auditLog);
        when(auditLogRepository.findByUserIdOrderByTimestampDesc(userId)).thenReturn(logs);

        // When
        List<AuditLog> result = auditLogService.getUserHistory(userId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(auditLogRepository, times(1)).findByUserIdOrderByTimestampDesc(userId);
    }

    @Test
    void testGetAuditLogsByHotel() {
        // Given
        List<AuditLog> logs = Arrays.asList(auditLog);
        when(auditLogRepository.findByHotelIdOrderByTimestampDesc(hotelId)).thenReturn(logs);

        // When
        List<AuditLog> result = auditLogService.getHotelHistory(hotelId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(auditLogRepository, times(1)).findByHotelIdOrderByTimestampDesc(hotelId);
    }

    @Test
    void testGetAllAuditLogs() {
        // Given
        List<AuditLog> logs = Arrays.asList(auditLog);
        when(auditLogRepository.findAll()).thenReturn(logs);

        // When
        List<AuditLog> result = auditLogService.getAllLogs();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(auditLogRepository, times(1)).findAll();
    }
}
