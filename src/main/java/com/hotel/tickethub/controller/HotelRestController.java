package com.hotel.tickethub.controller;

import com.hotel.tickethub.dto.HotelDTO;
import com.hotel.tickethub.dto.HotelRequest;
import com.hotel.tickethub.model.Hotel;
import com.hotel.tickethub.service.HotelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/hotels")
@RequiredArgsConstructor
@CrossOrigin(origins = {
        "http://localhost:8080",
        "http://localhost:8081",
        "http://localhost:5173",
        "http://192.168.58.1:5173",
        "http://13.50.221.51",
        "http://13.50.221.51:80",
        "http://13.50.221.51:8080",
        "https://13.50.221.51",
        "https://13.50.221.51:443"
})
public class HotelRestController {

    private final HotelService hotelService;

    /**
     * GET /api/hotels/public - Récupérer les hôtels publics (sans authentification)
     */
    @GetMapping("/public")
    public ResponseEntity<List<HotelDTO>> getPublicHotels() {
        try {
            List<HotelDTO> hotels = hotelService.getAllHotelsDTO();
            // Toujours retourner une liste, même si vide
            if (hotels == null) {
                hotels = List.of();
            }
            return ResponseEntity.ok(hotels);
        } catch (Exception e) {
            // Log l'erreur pour le débogage
            System.err.println("Error in getPublicHotels: " + e.getMessage());
            e.printStackTrace();
            // Retourner une liste vide au lieu d'une erreur 500 pour éviter de casser le
            // frontend
            return ResponseEntity.ok(List.of());
        }
    }

    /**
     * GET /api/hotels - Récupérer tous les hôtels (SuperAdmin uniquement)
     */
    @GetMapping
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<List<HotelDTO>> getAllHotels() {
        return ResponseEntity.ok(hotelService.getAllHotelsDTO());
    }

    /**
     * GET /api/hotels/{id} - Récupérer un hôtel par ID
     * Retourne un DTO pour éviter les références circulaires
     */
    @GetMapping("/{id}")
    public ResponseEntity<HotelDTO> getHotelById(@PathVariable UUID id) {
        return hotelService.getHotelById(id)
                .map(hotel -> ResponseEntity.ok(hotelService.convertToDTO(hotel)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * POST /api/hotels - Créer un nouvel hôtel (SuperAdmin uniquement)
     */
    @PostMapping
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<Hotel> createHotel(@RequestBody HotelRequest request) {
        Hotel hotel = hotelService.createHotel(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(hotel);
    }

    /**
     * PUT /api/hotels/{id} - Mettre à jour un hôtel
     */
    @PutMapping("/{id}")
    public ResponseEntity<Hotel> updateHotel(
            @PathVariable UUID id,
            @RequestBody HotelRequest request) {
        return hotelService.updateHotel(id, request)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * DELETE /api/hotels/{id} - Supprimer un hôtel
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHotel(@PathVariable UUID id) {
        hotelService.deleteHotel(id);
        return ResponseEntity.noContent().build();
    }
}
