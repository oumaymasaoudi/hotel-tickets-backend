package com.hotel.tickethub.controller;

import com.hotel.tickethub.dto.AuditLogRequest;
import com.hotel.tickethub.dto.PaymentRequest;
import com.hotel.tickethub.dto.PaymentResponse;
import com.hotel.tickethub.model.Payment;
import com.hotel.tickethub.model.User;
import com.hotel.tickethub.service.PaymentService;
import com.hotel.tickethub.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

/**
 * Endpoints pour la gestion des paiements
 * Règle 4, 10, 11: Gestion des paiements et historique
 */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@CrossOrigin(origins = {
        "http://localhost:8080",
        "http://localhost:8081",
        "http://localhost:5173",
        "http://192.168.58.1:5173"
})
public class PaymentRestController {

    private final PaymentService paymentService;
    private final AuditLogService auditLogService;

    /**
     * GET /api/payments/hotel/{hotelId}
     * Récupérer l'historique des paiements d'un hôtel
     * Accessible par: Admin de l'hôtel + SuperAdmin
     * Règle 11: Historique des paiements
     */
    @GetMapping("/hotel/{hotelId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<List<PaymentResponse>> getPaymentHistory(@PathVariable UUID hotelId) {
        List<PaymentResponse> payments = paymentService.getPaymentHistory(hotelId).stream()
            .map(this::toPaymentResponse)
            .toList();
        return ResponseEntity.ok(payments);
    }

    /**
     * GET /api/payments/hotel/{hotelId}/last
     * Récupérer le dernier paiement d'un hôtel
     */
    @GetMapping("/hotel/{hotelId}/last")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<PaymentResponse> getLastPayment(@PathVariable UUID hotelId) {
        return paymentService.getLastPayment(hotelId)
            .map(p -> ResponseEntity.ok(toPaymentResponse(p)))
            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * GET /api/payments/hotel/{hotelId}/status
     * Vérifier si le paiement d'un hôtel est à jour
     * Règle 4: Vérification automatique
     */
    @GetMapping("/hotel/{hotelId}/status")
    public ResponseEntity<PaymentStatusResponse> checkPaymentStatus(@PathVariable UUID hotelId) {
        boolean isUpToDate = paymentService.isPaymentUpToDate(hotelId);
        return ResponseEntity.ok(
            PaymentStatusResponse.builder()
                .hotelId(hotelId)
                .isUpToDate(isUpToDate)
                .message(isUpToDate ? "Paiement à jour" : "Paiement en retard")
                .build()
        );
    }

    /**
     * GET /api/payments/hotel/{hotelId}/period?startDate=2025-01-01&endDate=2025-01-31
     * Récupérer les paiements d'une période
     * Règle 14: Rapports automatiques
     */
    @GetMapping("/hotel/{hotelId}/period")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<PeriodPaymentResponse> getPaymentsByPeriod(
            @PathVariable UUID hotelId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        List<PaymentResponse> payments = paymentService.getPaymentsByPeriod(hotelId, start, end).stream()
            .map(this::toPaymentResponse)
            .toList();

        double totalAmount = paymentService.getTotalPaymentAmount(hotelId, start, end);

        return ResponseEntity.ok(
            PeriodPaymentResponse.builder()
                .hotelId(hotelId)
                .startDate(startDate)
                .endDate(endDate)
                .payments(payments)
                .totalAmount(totalAmount)
                .count(payments.size())
                .build()
        );
    }

    /**
     * POST /api/payments/hotel/{hotelId}
     * Enregistrer un nouveau paiement
     * Accessible par: SuperAdmin uniquement
     * Règle 4 & 10: Enregistrement des paiements
     */
    @PostMapping("/hotel/{hotelId}")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<PaymentResponse> createPayment(
            @PathVariable UUID hotelId,
            @RequestBody PaymentRequest request,
            Authentication authentication) {

        Payment payment = new Payment();
        payment.setAmount(request.getAmount());
        payment.setNextPaymentDate(request.getNextPaymentDate());
        payment.setPaymentMethod(request.getPaymentMethod());
        payment.setPaymentReference(request.getPaymentReference());
        payment.setNotes(request.getNotes());

        Payment created = paymentService.createPayment(hotelId, payment);

        // Log l'action
        User user = (User) authentication.getPrincipal(); // À adapter selon implémentation
        AuditLogRequest auditRequest = AuditLogRequest.builder()
                .user(user)
                .action(AuditLogService.ActionType.CREATE_PAYMENT)
                .entityType(AuditLogService.EntityType.PAYMENT)
                .entityId(created.getId())
                .hotel(created.getHotel())
                .changes(request)
                .description("Nouveau paiement enregistré")
                .ipAddress(getClientIp())
                .build();
        auditLogService.logAction(auditRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(toPaymentResponse(created));
    }

    /**
     * PUT /api/payments/{paymentId}
     * Mettre à jour un paiement
     * Accessible par: SuperAdmin uniquement
     */
    @PutMapping("/{paymentId}")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<PaymentResponse> updatePayment(
            @PathVariable UUID paymentId,
            @RequestBody PaymentRequest request) {

        // À implémenter selon les besoins
        return ResponseEntity.notFound().build();
    }

    /**
     * GET /api/payments/overdue
     * Récupérer les paiements en retard
     * Accessible par: SuperAdmin
     * Règle 10: Avertissement et accès restreint
     */
    @GetMapping("/overdue")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<List<PaymentResponse>> getOverduePayments() {
        List<PaymentResponse> payments = paymentService.getOverduePayments().stream()
            .map(this::toPaymentResponse)
            .toList();
        return ResponseEntity.ok(payments);
    }

    /**
     * GET /api/payments/all
     * Récupérer tous les paiements (pour SuperAdmin)
     * Accessible par: SuperAdmin uniquement
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<List<PaymentResponse>> getAllPayments() {
        List<PaymentResponse> payments = paymentService.getAllPayments().stream()
            .map(this::toPaymentResponse)
            .toList();
        return ResponseEntity.ok(payments);
    }

    // ========== DTO Conversion ==========

    private PaymentResponse toPaymentResponse(Payment payment) {
        return PaymentResponse.builder()
            .id(payment.getId())
            .hotelId(payment.getHotel().getId())
            .hotelName(payment.getHotel().getName())
            .amount(payment.getAmount())
            .paymentDate(payment.getPaymentDate())
            .nextPaymentDate(payment.getNextPaymentDate())
            .status(payment.getStatus().name())
            .paymentMethod(payment.getPaymentMethod())
            .paymentReference(payment.getPaymentReference())
            .notes(payment.getNotes())
            .build();
    }

    private static final String DEFAULT_CLIENT_IP = "0.0.0.0";

    private String getClientIp() {
        // À implémenter pour récupérer l'IP du client
        return DEFAULT_CLIENT_IP;
    }
}

// ========== DTOs ==========

@lombok.Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
class PaymentStatusResponse {
    private UUID hotelId;
    private boolean isUpToDate;
    private String message;
}

@lombok.Data
@lombok.Builder
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
class PeriodPaymentResponse {
    private UUID hotelId;
    private LocalDate startDate;
    private LocalDate endDate;
    private List<PaymentResponse> payments;
    private double totalAmount;
    private int count;
}
