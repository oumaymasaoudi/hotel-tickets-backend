package com.hotel.tickethub.controller;

import com.hotel.tickethub.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Controller pour les rapports
 * Règle 14 & 15: Rapports automatiques et statistiques globales
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    /**
     * GET /api/reports/hotel/{hotelId}/monthly
     * Générer un rapport mensuel pour un hôtel (Admin uniquement)
     */
    @GetMapping("/hotel/{hotelId}/monthly")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<Map<String, Object>> getMonthlyReport(
            @PathVariable UUID hotelId,
            @RequestParam(defaultValue = "0") int year,
            @RequestParam(defaultValue = "0") int month) {

        if (year == 0)
            year = LocalDateTime.now().getYear();
        if (month == 0)
            month = LocalDateTime.now().getMonthValue() - 1;

        if (month == 0) {
            month = 12;
            year--;
        }

        return ResponseEntity.ok(reportService.generateMonthlyReport(hotelId, year, month));
    }

    /**
     * GET /api/reports/hotel/{hotelId}/weekly
     * Générer un rapport hebdomadaire pour un hôtel (Admin uniquement)
     */
    @GetMapping("/hotel/{hotelId}/weekly")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<Map<String, Object>> getWeeklyReport(
            @PathVariable UUID hotelId,
            @RequestParam(required = false) String startDate) {

        LocalDateTime weekStart;
        if (startDate != null) {
            weekStart = LocalDateTime.parse(startDate);
        } else {
            weekStart = LocalDateTime.now().minusWeeks(1).withHour(0).withMinute(0).withSecond(0);
        }

        return ResponseEntity.ok(reportService.generateWeeklyReport(hotelId, weekStart));
    }

    /**
     * GET /api/reports/hotel/{hotelId}/daily
     * Générer un rapport quotidien pour un hôtel (Admin uniquement)
     */
    @GetMapping("/hotel/{hotelId}/daily")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPERADMIN')")
    public ResponseEntity<Map<String, Object>> getDailyReport(
            @PathVariable UUID hotelId,
            @RequestParam(required = false) String date) {

        LocalDateTime reportDate;
        if (date != null) {
            reportDate = LocalDateTime.parse(date);
        } else {
            reportDate = LocalDateTime.now();
        }

        return ResponseEntity.ok(reportService.generateDailyReport(hotelId, reportDate));
    }

    /**
     * GET /api/reports/global
     * Générer un rapport consolidé global (SuperAdmin uniquement)
     */
    @GetMapping("/global")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<Map<String, Object>> getGlobalReport(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        LocalDateTime start;
        LocalDateTime end;

        if (startDate != null && endDate != null) {
            start = LocalDateTime.parse(startDate);
            end = LocalDateTime.parse(endDate);
        } else {
            // Par défaut: dernier mois
            end = LocalDateTime.now();
            start = end.minusMonths(1);
        }

        return ResponseEntity.ok(reportService.generateGlobalReport(start, end));
    }
}
