package com.hotel.tickethub.repository;

import com.hotel.tickethub.model.Ticket;
import com.hotel.tickethub.model.enums.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, UUID> {
    Optional<Ticket> findByTicketNumber(String ticketNumber);

    List<Ticket> findByHotelId(UUID hotelId);
    
    // Pagination pour les tickets d'un hôtel
    Page<Ticket> findByHotelId(UUID hotelId, Pageable pageable);

    List<Ticket> findByStatus(TicketStatus status);
    
    // Pagination pour les tickets par statut
    Page<Ticket> findByStatus(TicketStatus status, Pageable pageable);

    List<Ticket> findByAssignedTechnicianId(UUID technicianId);
    
    // Pagination pour les tickets d'un technicien
    Page<Ticket> findByAssignedTechnicianId(UUID technicianId, Pageable pageable);

    List<Ticket> findByClientEmail(String clientEmail);
    
    // Pagination pour les tickets d'un client
    Page<Ticket> findByClientEmail(String clientEmail, Pageable pageable);

    // Pour les rapports
    @Query("SELECT t FROM Ticket t WHERE t.createdAt BETWEEN :startDate AND :endDate")
    List<Ticket> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT t FROM Ticket t WHERE t.hotel.id = :hotelId AND t.createdAt BETWEEN :startDate AND :endDate")
    List<Ticket> findByHotelIdAndCreatedAtBetween(@Param("hotelId") UUID hotelId,
            @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
