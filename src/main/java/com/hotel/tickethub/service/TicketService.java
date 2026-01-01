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

@Service
@RequiredArgsConstructor
public class TicketService {

    private static final String TICKET_NOT_FOUND_MESSAGE = "Ticket not found";
    private static final String UPLOAD_DIR = "./uploads";

    private final TicketRepository ticketRepository;
    private final HotelRepository hotelRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final TicketImageRepository ticketImageRepository;
    private final TicketHistoryRepository ticketHistoryRepository;
    private final PlanRepository planRepository;

    @Transactional
    public TicketResponse createTicket(CreateTicketRequest request, List<MultipartFile> images) {
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
        ticket.setIsUrgent(Boolean.TRUE.equals(request.getIsUrgent()));

        // Calculate SLA deadline based on hotel plan
        int slaHours = hotel.getPlan().getSlaHours();
        ticket.setSlaDeadline(LocalDateTime.now().plusHours(slaHours));

        // L'assignation sera faite manuellement par l'admin de l'hôtel
        // Pas d'assignation automatique

        ticket = ticketRepository.save(ticket);

        // Save images if provided
        if (images != null && !images.isEmpty()) {
            for (MultipartFile image : images) {
                saveTicketImage(ticket, image);
            }
        }

        return convertToResponse(ticket);
    }

    public TicketResponse getTicketByNumber(String ticketNumber) {
        Ticket ticket = ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new RuntimeException(TICKET_NOT_FOUND_MESSAGE));
        return convertToResponse(ticket);
    }

    public TicketResponse getTicketById(UUID ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException(TICKET_NOT_FOUND_MESSAGE));
        return convertToResponse(ticket);
    }

    public List<TicketResponse> getTicketsByHotel(UUID hotelId) {
        return ticketRepository.findByHotelId(hotelId).stream()
                .map(this::convertToResponse)
                .toList();
    }

    public List<TicketResponse> getTicketsByTechnician(UUID technicianId) {
        return ticketRepository.findByAssignedTechnicianId(technicianId).stream()
                .map(this::convertToResponse)
                .toList();
    }

    public List<TicketResponse> getTicketsByEmail(String email) {
        return ticketRepository.findByClientEmail(email).stream()
                .map(this::convertToResponse)
                .toList();
    }

    /**
     * Récupérer tous les tickets (pour SuperAdmin)
     */
    public List<TicketResponse> getAllTickets() {
        return ticketRepository.findAll().stream()
                .map(this::convertToResponse)
                .toList();
    }

    @Transactional
    public TicketResponse updateTicketStatus(UUID ticketId, UpdateTicketStatusRequest request, UUID userId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException(TICKET_NOT_FOUND_MESSAGE));

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
                .orElseThrow(() -> new RuntimeException(TICKET_NOT_FOUND_MESSAGE));

        if (images != null && !images.isEmpty()) {
            for (MultipartFile image : images) {
                saveTicketImage(ticket, image);
            }
        }

        return convertToResponse(ticket);
    }

    @Transactional
    public void deleteTicketImage(UUID ticketId, UUID imageId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException(TICKET_NOT_FOUND_MESSAGE));

        TicketImage image = ticketImageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found"));

        // Verify the image belongs to the ticket
        if (!image.getTicket().getId().equals(ticketId)) {
            throw new RuntimeException("Image does not belong to this ticket");
        }

        // Delete the physical file
        try {
            Path filePath = Paths.get(image.getStoragePath());
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
        } catch (IOException e) {
            // Log but don't fail if file deletion fails
            System.err.println("Failed to delete image file: " + e.getMessage());
        }

        // Delete the database record
        ticketImageRepository.delete(image);

        // Log the deletion
        logTicketHistory(ticket, "IMAGE_DELETED", image.getFileName(), null, null);
    }

    private void saveTicketImage(Ticket ticket, MultipartFile file) {
        // Validation du fichier
        validateImageFile(file);

        try {
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

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

    /**
     * Valider un fichier image avant l'upload
     * Vérifie le type MIME, la taille et l'extension
     */
    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Le fichier est vide");
        }

        // Vérifier la taille (max 10MB)
        long maxSize = 10 * 1024 * 1024; // 10MB
        if (file.getSize() > maxSize) {
            throw new RuntimeException("Le fichier est trop volumineux. Taille maximale: 10MB");
        }

        // Vérifier le type MIME
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("Le fichier doit être une image");
        }

        // Vérifier l'extension
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null) {
            String extension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
            List<String> allowedExtensions = List.of("jpg", "jpeg", "png", "gif", "webp");
            if (!allowedExtensions.contains(extension)) {
                throw new RuntimeException("Extension de fichier non autorisée. Extensions autorisées: " + allowedExtensions);
            }
        }

        // Vérifier que le nom de fichier n'est pas vide
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new RuntimeException("Le nom de fichier est invalide");
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
                        .storagePath(img.getStoragePath())
                        .fileName(img.getFileName())
                        .build())
                .toList();
        response.setImages(imageDTOs);

        return response;
    }
}
