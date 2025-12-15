package com.hotel.tickethub.controller;

import com.hotel.tickethub.dto.CreateTicketRequest;
import com.hotel.tickethub.dto.TicketResponse;
import com.hotel.tickethub.dto.UpdateTicketStatusRequest;
import com.hotel.tickethub.service.TicketService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping("/public")
    public ResponseEntity<TicketResponse> createTicket(
            @Valid @RequestPart("ticket") CreateTicketRequest request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images) {
        try {
            return ResponseEntity.ok(ticketService.createTicket(request, images));
        } catch (RuntimeException e) {
            // Retourner une erreur 400 avec le message d'erreur
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/public/{ticketNumber}")
    public ResponseEntity<TicketResponse> getTicketByNumber(@PathVariable String ticketNumber) {
        return ResponseEntity.ok(ticketService.getTicketByNumber(ticketNumber));
    }

    @GetMapping("/public/email/{email}")
    public ResponseEntity<List<TicketResponse>> getTicketsByEmail(@PathVariable String email) {
        return ResponseEntity.ok(ticketService.getTicketsByEmail(email));
    }

    @PostMapping("/{ticketId}/images")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TECHNICIAN') or hasRole('SUPERADMIN')")
    public ResponseEntity<TicketResponse> addImagesToTicket(
            @PathVariable UUID ticketId,
            @RequestPart("images") List<MultipartFile> images) {
        return ResponseEntity.ok(ticketService.addImagesToTicket(ticketId, images));
    }

    @GetMapping("/hotel/{hotelId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPERADMIN')")
    public ResponseEntity<List<TicketResponse>> getTicketsByHotel(@PathVariable UUID hotelId) {
        return ResponseEntity.ok(ticketService.getTicketsByHotel(hotelId));
    }

    @GetMapping("/technician/{technicianId}")
    @PreAuthorize("hasRole('TECHNICIAN')")
    public ResponseEntity<List<TicketResponse>> getTicketsByTechnician(@PathVariable UUID technicianId) {
        return ResponseEntity.ok(ticketService.getTicketsByTechnician(technicianId));
    }

    /**
     * GET /api/tickets/all - Récupérer tous les tickets (SuperAdmin uniquement)
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<List<TicketResponse>> getAllTickets() {
        return ResponseEntity.ok(ticketService.getAllTickets());
    }

    @PatchMapping("/{ticketId}/status")
    @PreAuthorize("hasAnyRole('TECHNICIAN', 'ADMIN', 'SUPERADMIN')")
    public ResponseEntity<TicketResponse> updateTicketStatus(
            @PathVariable UUID ticketId,
            @Valid @RequestBody UpdateTicketStatusRequest request,
            @RequestParam UUID userId) {
        return ResponseEntity.ok(ticketService.updateTicketStatus(ticketId, request, userId));
    }
}
