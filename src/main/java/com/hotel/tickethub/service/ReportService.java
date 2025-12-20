package com.hotel.tickethub.service;

import com.hotel.tickethub.model.Hotel;
import com.hotel.tickethub.model.Ticket;
import com.hotel.tickethub.model.User;
import com.hotel.tickethub.model.enums.TicketStatus;
import com.hotel.tickethub.repository.HotelRepository;
import com.hotel.tickethub.repository.TicketRepository;
import com.hotel.tickethub.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service pour générer des rapports automatiques
 * Règle 14: Rapports automatiques
 */
@Service
@RequiredArgsConstructor
public class ReportService {

    private static final String TOTAL_TICKETS_KEY = "totalTickets";
    private static final String OPEN_TICKETS_KEY = "openTickets";
    private static final String IN_PROGRESS_TICKETS_KEY = "inProgressTickets";
    private static final String RESOLVED_TICKETS_KEY = "resolvedTickets";
    private static final String HOTEL_NAME_KEY = "hotelName";
    private static final String AVERAGE_RESOLUTION_TIME_HOURS_KEY = "averageResolutionTimeHours";

    private final TicketRepository ticketRepository;
    private final HotelRepository hotelRepository;
    private final UserRepository userRepository;
    private final PaymentService paymentService;

    /**
     * Générer un rapport mensuel pour un hôtel
     */
    public Map<String, Object> generateMonthlyReport(UUID hotelId, int year, int month) {
        LocalDateTime startDate = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime endDate = startDate.plusMonths(1).minusSeconds(1);

        return generateReport(hotelId, startDate, endDate, "Mensuel");
    }

    /**
     * Générer un rapport hebdomadaire pour un hôtel
     */
    public Map<String, Object> generateWeeklyReport(UUID hotelId, LocalDateTime weekStart) {
        LocalDateTime weekEnd = weekStart.plusWeeks(1).minusSeconds(1);
        return generateReport(hotelId, weekStart, weekEnd, "Hebdomadaire");
    }

    /**
     * Générer un rapport quotidien pour un hôtel
     */
    public Map<String, Object> generateDailyReport(UUID hotelId, LocalDateTime date) {
        LocalDateTime startDate = date.withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endDate = date.withHour(23).withMinute(59).withSecond(59);
        return generateReport(hotelId, startDate, endDate, "Quotidien");
    }

    /**
     * Générer un rapport consolidé pour tous les hôtels (SuperAdmin)
     */
    public Map<String, Object> generateGlobalReport(LocalDateTime startDate, LocalDateTime endDate) {
        List<Hotel> hotels = hotelRepository.findAll();

        Map<String, Object> report = new HashMap<>();
        report.put("type", "Global");
        report.put("period", Map.of("start", startDate, "end", endDate));
        report.put("generatedAt", LocalDateTime.now());

        // Statistiques globales
        List<Ticket> allTickets = ticketRepository.findByCreatedAtBetween(startDate, endDate);
        report.put(TOTAL_TICKETS_KEY, allTickets.size());
        report.put(OPEN_TICKETS_KEY, countByStatus(allTickets, TicketStatus.OPEN));
        report.put(IN_PROGRESS_TICKETS_KEY, countByStatus(allTickets, TicketStatus.IN_PROGRESS));
        report.put(RESOLVED_TICKETS_KEY, countByStatus(allTickets, TicketStatus.RESOLVED));
        report.put("closedTickets", countByStatus(allTickets, TicketStatus.CLOSED));
        report.put("urgentTickets", allTickets.stream().filter(Ticket::getIsUrgent).count());

        // Statistiques par hôtel
        List<Map<String, Object>> hotelStats = hotels.stream()
                .map(hotel -> generateHotelStats(hotel, startDate, endDate))
                .toList();
        report.put("hotels", hotelStats);

        // Performances techniciens
        List<Map<String, Object>> technicianStats = generateTechnicianStats(startDate, endDate);
        report.put("technicians", technicianStats);

        // Paiements
        report.put("totalHotels", hotels.size());
        report.put("activeHotels", hotels.stream().filter(Hotel::getIsActive).count());
        report.put("overduePayments", paymentService.getOverduePayments().size());

        return report;
    }

    private Map<String, Object> generateReport(UUID hotelId, LocalDateTime startDate, LocalDateTime endDate,
            String type) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new RuntimeException("Hôtel non trouvé"));

        Map<String, Object> report = new HashMap<>();
        report.put("type", type);
        report.put("hotelId", hotelId);
        report.put(HOTEL_NAME_KEY, hotel.getName());
        report.put("period", Map.of("start", startDate, "end", endDate));
        report.put("generatedAt", LocalDateTime.now());

        // Tickets dans la période
        List<Ticket> tickets = ticketRepository.findByHotelIdAndCreatedAtBetween(hotelId, startDate, endDate);

        report.put(TOTAL_TICKETS_KEY, tickets.size());
        report.put(OPEN_TICKETS_KEY, countByStatus(tickets, TicketStatus.OPEN));
        report.put(IN_PROGRESS_TICKETS_KEY, countByStatus(tickets, TicketStatus.IN_PROGRESS));
        report.put(RESOLVED_TICKETS_KEY, countByStatus(tickets, TicketStatus.RESOLVED));
        report.put("closedTickets", countByStatus(tickets, TicketStatus.CLOSED));
        report.put("urgentTickets", tickets.stream().filter(Ticket::getIsUrgent).count());

        // Temps moyen de résolution
        double avgResolutionTime = calculateAverageResolutionTime(tickets);
        report.put(AVERAGE_RESOLUTION_TIME_HOURS_KEY, avgResolutionTime);

        // Performances techniciens
        List<Map<String, Object>> technicianStats = generateTechnicianStatsForHotel(hotelId, startDate, endDate);
        report.put("technicians", technicianStats);

        // Paiements récents et à venir
        List<Map<String, Object>> recentPayments = paymentService.getPaymentHistory(hotelId).stream()
                .limit(5)
                .map(p -> {
                    Map<String, Object> paymentMap = new HashMap<>();
                    paymentMap.put("date", p.getPaymentDate());
                    paymentMap.put("amount", p.getAmount());
                    paymentMap.put("status", p.getStatus());
                    return paymentMap;
                })
                .toList();
        report.put("recentPayments", recentPayments);

        // Prochain paiement
        if (hotel.getNextPaymentDate() != null) {
            report.put("nextPaymentDate", hotel.getNextPaymentDate());
            report.put("daysUntilNextPayment",
                    java.time.temporal.ChronoUnit.DAYS.between(LocalDateTime.now(), hotel.getNextPaymentDate()));
        }

        return report;
    }

    private long countByStatus(List<Ticket> tickets, TicketStatus status) {
        return tickets.stream().filter(t -> t.getStatus() == status).count();
    }

    private double calculateAverageResolutionTime(List<Ticket> tickets) {
        return tickets.stream()
                .filter(t -> t.getResolvedAt() != null && t.getCreatedAt() != null)
                .mapToLong(t -> java.time.temporal.ChronoUnit.HOURS.between(t.getCreatedAt(), t.getResolvedAt()))
                .average()
                .orElse(0.0);
    }

    private List<Map<String, Object>> generateTechnicianStatsForHotel(UUID hotelId, LocalDateTime startDate,
            LocalDateTime endDate) {
        List<Ticket> tickets = ticketRepository.findByHotelIdAndCreatedAtBetween(hotelId, startDate, endDate);

        Map<UUID, List<Ticket>> ticketsByTechnician = tickets.stream()
                .filter(t -> t.getAssignedTechnician() != null)
                .collect(Collectors.groupingBy(t -> t.getAssignedTechnician().getId()));

        return ticketsByTechnician.entrySet().stream()
                .map(entry -> {
                    User technician = userRepository.findById(entry.getKey()).orElse(null);
                    List<Ticket> techTickets = entry.getValue();

                    Map<String, Object> stats = new HashMap<>();
                    stats.put("technicianId", entry.getKey());
                    stats.put("technicianName", technician != null ? technician.getFullName() : "Inconnu");
                    stats.put(TOTAL_TICKETS_KEY, techTickets.size());
                    stats.put(RESOLVED_TICKETS_KEY, countByStatus(techTickets, TicketStatus.RESOLVED));
                    stats.put(IN_PROGRESS_TICKETS_KEY, countByStatus(techTickets, TicketStatus.IN_PROGRESS));
                    stats.put(AVERAGE_RESOLUTION_TIME_HOURS_KEY, calculateAverageResolutionTime(techTickets));

                    return stats;
                })
                .toList();
    }

    private List<Map<String, Object>> generateTechnicianStats(LocalDateTime startDate, LocalDateTime endDate) {
        List<Ticket> allTickets = ticketRepository.findByCreatedAtBetween(startDate, endDate);

        Map<UUID, List<Ticket>> ticketsByTechnician = allTickets.stream()
                .filter(t -> t.getAssignedTechnician() != null)
                .collect(Collectors.groupingBy(t -> t.getAssignedTechnician().getId()));

        return ticketsByTechnician.entrySet().stream()
                .map(entry -> {
                    User technician = userRepository.findById(entry.getKey()).orElse(null);
                    List<Ticket> techTickets = entry.getValue();

                    Map<String, Object> stats = new HashMap<>();
                    stats.put("technicianId", entry.getKey());
                    stats.put("technicianName", technician != null ? technician.getFullName() : "Inconnu");
                    stats.put("hotelName", techTickets.stream()
                            .findFirst()
                            .map(t -> t.getHotel().getName())
                            .orElse("N/A"));
                    stats.put(TOTAL_TICKETS_KEY, techTickets.size());
                    stats.put(RESOLVED_TICKETS_KEY, countByStatus(techTickets, TicketStatus.RESOLVED));
                    stats.put(AVERAGE_RESOLUTION_TIME_HOURS_KEY, calculateAverageResolutionTime(techTickets));

                    return stats;
                })
                .toList();
    }

    private Map<String, Object> generateHotelStats(Hotel hotel, LocalDateTime startDate, LocalDateTime endDate) {
        List<Ticket> tickets = ticketRepository.findByHotelIdAndCreatedAtBetween(hotel.getId(), startDate, endDate);

        Map<String, Object> stats = new HashMap<>();
        stats.put("hotelId", hotel.getId());
        stats.put(HOTEL_NAME_KEY, hotel.getName());
        stats.put(TOTAL_TICKETS_KEY, tickets.size());
        stats.put(OPEN_TICKETS_KEY, countByStatus(tickets, TicketStatus.OPEN));
        stats.put(RESOLVED_TICKETS_KEY, countByStatus(tickets, TicketStatus.RESOLVED));
        stats.put("isActive", hotel.getIsActive());

        return stats;
    }
}
