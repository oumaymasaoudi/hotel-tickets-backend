package com.hotel.tickethub.service;

import com.hotel.tickethub.dto.CreateTicketRequest;
import com.hotel.tickethub.dto.TicketImageDTO;
import com.hotel.tickethub.dto.TicketResponse;
import com.hotel.tickethub.dto.UpdateTicketStatusRequest;
import com.hotel.tickethub.model.*;
import com.hotel.tickethub.model.enums.TicketStatus;
import com.hotel.tickethub.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final HotelRepository hotelRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final TicketImageRepository ticketImageRepository;
    private final TicketHistoryRepository ticketHistoryRepository;
    private final PlanRepository planRepository;

    private static final String UPLOAD_DIR = "./uploads";

    @Transactional
    public TicketResponse createTicket(CreateTicketRequest request, List<MultipartFile> images) {
        try {
            Hotel hotel = hotelRepository.findById(request.getHotelId())
                    .orElseThrow(() -> new RuntimeException("Hotel not found with ID: " + request.getHotelId()));

            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found with ID: " + request.getCategoryId()));

            // Vérifier que l'hôtel a un plan
            if (hotel.getPlan() == null) {
                throw new RuntimeException(
                        "Hotel does not have a subscription plan. Please assign a plan to the hotel.");
            }

            Ticket ticket = new Ticket();
            ticket.setTicketNumber(generateTicketNumber());
            ticket.setHotel(hotel);
            ticket.setCategory(category);
            ticket.setClientEmail(request.getClientEmail());
            ticket.setClientPhone(request.getClientPhone());
            ticket.setDescription(request.getDescription());
            ticket.setStatus(TicketStatus.OPEN);
            ticket.setIsUrgent(request.getIsUrgent() != null ? request.getIsUrgent() : false);

            // Calculate SLA deadline based on hotel plan
            int slaHours = hotel.getPlan().getSlaHours();
            ticket.setSlaDeadline(LocalDateTime.now().plusHours(slaHours));

            ticket = ticketRepository.save(ticket);

            // Save images if provided
            if (images != null && !images.isEmpty()) {
                for (MultipartFile image : images) {
                    saveTicketImage(ticket, image);
                }
            }

            return convertToResponse(ticket);
        } catch (RuntimeException e) {
            throw e;
        }
    }

    public TicketResponse getTicketByNumber(String ticketNumber) {
        Ticket ticket = ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
        return convertToResponse(ticket);
    }

    public TicketResponse getTicketById(UUID ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
        return convertToResponse(ticket);
    }

    public List<TicketResponse> getTicketsByHotel(UUID hotelId) {
        return ticketRepository.findByHotelId(hotelId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<TicketResponse> getTicketsByTechnician(UUID technicianId) {
        return ticketRepository.findByAssignedTechnicianId(technicianId).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    public List<TicketResponse> getTicketsByEmail(String email) {
        return ticketRepository.findByClientEmail(email).stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Récupérer tous les tickets (pour SuperAdmin)
     */
    public List<TicketResponse> getAllTickets() {
        return ticketRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public TicketResponse updateTicketStatus(UUID ticketId, UpdateTicketStatusRequest request, UUID userId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        TicketStatus oldStatus = ticket.getStatus();
        ticket.setStatus(request.getStatus());

        if (request.getTechnicianId() != null) {
            User technician = userRepository.findById(request.getTechnicianId())
                    .orElseThrow(() -> new RuntimeException("Technician not found"));
            ticket.setAssignedTechnician(technician);
        }

        if (request.getStatus() == TicketStatus.RESOLVED || request.getStatus() == TicketStatus.CLOSED) {
            ticket.setResolvedAt(LocalDateTime.now());
        }

        ticket = ticketRepository.save(ticket);

        // Log history
        User performedBy = userRepository.findById(userId).orElse(null);
        logTicketHistory(ticket, "STATUS_CHANGE", oldStatus.name(), request.getStatus().name(), performedBy);

        return convertToResponse(ticket);
    }

    @Transactional
    public TicketResponse addImagesToTicket(UUID ticketId, List<MultipartFile> images) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        if (images != null && !images.isEmpty()) {
            for (MultipartFile image : images) {
                saveTicketImage(ticket, image);
            }
        }

        return convertToResponse(ticket);
    }

    private void saveTicketImage(Ticket ticket, MultipartFile file) {
        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            // String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            // Path filePath = uploadPath.resolve(fileName);
            // Sanitize filename: extract only the base filename to prevent path traversal
            String originalFilename = file.getOriginalFilename();
            String safeFilename = originalFilename != null
                    ? Paths.get(originalFilename).getFileName().toString()
                    : "file";
            // Remove any remaining dangerous characters
            safeFilename = safeFilename.replaceAll("[^a-zA-Z0-9._-]", "_");

            String fileName = UUID.randomUUID() + "_" + safeFilename;
            Path filePath = uploadPath.resolve(fileName).normalize();

            // Additional security check: ensure the resolved path is within the upload
            // directory
            if (!filePath.startsWith(uploadPath.normalize())) {
                throw new SecurityException("Invalid file path detected");
            }

            Files.copy(file.getInputStream(), filePath);

            TicketImage ticketImage = new TicketImage();
            ticketImage.setTicket(ticket);
            ticketImage.setFileName(file.getOriginalFilename());
            ticketImage.setStoragePath(filePath.toString());
            ticketImageRepository.save(ticketImage);

        } catch (IOException e) {
            throw new RuntimeException("Failed to store image", e);
        }
    }

    private void logTicketHistory(Ticket ticket, String actionType, String oldValue, String newValue,
            User performedBy) {
        TicketHistory history = new TicketHistory();
        history.setTicket(ticket);
        history.setActionType(actionType);
        history.setOldValue(oldValue);
        history.setNewValue(newValue);
        history.setPerformedBy(performedBy);
        ticketHistoryRepository.save(history);
    }

    private String generateTicketNumber() {
        return "TKT-" + System.currentTimeMillis();
    }

    private TicketResponse convertToResponse(Ticket ticket) {
        TicketResponse response = new TicketResponse();
        response.setId(ticket.getId());
        response.setTicketNumber(ticket.getTicketNumber());
        response.setHotelId(ticket.getHotel().getId());
        response.setHotelName(ticket.getHotel().getName());
        response.setCategoryId(ticket.getCategory().getId());
        response.setCategoryName(ticket.getCategory().getName());
        response.setCategoryIcon(ticket.getCategory().getIcon());
        response.setCategoryColor(ticket.getCategory().getColor());
        response.setClientEmail(ticket.getClientEmail());
        response.setClientPhone(ticket.getClientPhone());
        response.setDescription(ticket.getDescription());
        response.setStatus(ticket.getStatus());
        response.setIsUrgent(ticket.getIsUrgent());

        if (ticket.getAssignedTechnician() != null) {
            response.setAssignedTechnicianId(ticket.getAssignedTechnician().getId());
            response.setAssignedTechnicianName(ticket.getAssignedTechnician().getFullName());
        }

        response.setSlaDeadline(ticket.getSlaDeadline());
        response.setResolvedAt(ticket.getResolvedAt());
        response.setCreatedAt(ticket.getCreatedAt());
        response.setUpdatedAt(ticket.getUpdatedAt());

        // Ajouter les images
        List<TicketImage> ticketImages = ticketImageRepository.findByTicketId(ticket.getId());
        List<TicketImageDTO> imageDTOs = ticketImages.stream()
                .map(img -> TicketImageDTO.builder()
                        .id(img.getId())
                        .storage_path(img.getStoragePath())
                        .file_name(img.getFileName())
                        .build())
                .collect(Collectors.toList());
        response.setImages(imageDTOs);

        return response;
    }
}
