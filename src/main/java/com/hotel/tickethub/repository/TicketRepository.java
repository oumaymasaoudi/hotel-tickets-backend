package com.hotel.tickethub.repository;

import com.hotel.tickethub.model.Ticket;
import com.hotel.tickethub.model.enums.TicketStatus;
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

    List<Ticket> findByStatus(TicketStatus status);

    List<Ticket> findByAssignedTechnicianId(UUID technicianId);

    List<Ticket> findByClientEmail(String clientEmail);

    // Pour les rapports
    @Query("SELECT t FROM Ticket t WHERE t.createdAt BETWEEN :startDate AND :endDate")
    List<Ticket> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT t FROM Ticket t WHERE t.hotel.id = :hotelId AND t.createdAt BETWEEN :startDate AND :endDate")
    List<Ticket> findByHotelIdAndCreatedAtBetween(@Param("hotelId") UUID hotelId,
            @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
