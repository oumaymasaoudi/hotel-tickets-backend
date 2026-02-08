package com.hotel.tickethub.service;

import com.hotel.tickethub.dto.HotelDTO;
import com.hotel.tickethub.dto.HotelRequest;
import com.hotel.tickethub.model.Hotel;
import com.hotel.tickethub.model.Plan;
import com.hotel.tickethub.repository.HotelRepository;
import com.hotel.tickethub.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class HotelService {

    private final HotelRepository hotelRepository;
    private final PlanRepository planRepository;

    public List<Hotel> getAllHotels() {
        return hotelRepository.findAll();
    }

    public List<HotelDTO> getAllHotelsDTO() {
        try {
            List<Hotel> hotels = hotelRepository.findAll();
            log.debug("Found {} hotels in database", hotels.size());

            if (hotels == null || hotels.isEmpty()) {
                log.info("No hotels found in database");
                return List.of(); // Retourner une liste vide au lieu de null
            }

            return hotels.stream()
                    .filter(hotel -> hotel != null) // Filtrer les hôtels null
                    .map(hotel -> {
                        try {
                            return convertToDTO(hotel);
                        } catch (Exception e) {
                            log.warn("Error converting hotel to DTO: hotelId={}, error={}",
                                    hotel != null ? hotel.getId() : "null", e.getMessage());
                            return null; // Retourner null pour cet hôtel, sera filtré ensuite
                        }
                    })
                    .filter(dto -> dto != null) // Filtrer les DTOs null en cas d'erreur
                    .toList();
        } catch (Exception e) {
            log.error("Error in getAllHotelsDTO: {}", e.getMessage(), e);
            // Retourner une liste vide au lieu de lancer une exception
            return List.of();
        }
    }

    public Optional<Hotel> getHotelById(UUID id) {
        return hotelRepository.findById(id);
    }

    public Hotel createHotel(HotelRequest request) {
        log.debug("createHotel: request={}", request);

        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom de l'hôtel est requis");
        }

        if (request.getPlanId() == null) {
            throw new IllegalArgumentException("Le plan d'abonnement est requis");
        }

        Hotel hotel = new Hotel();
        hotel.setName(request.getName());
        hotel.setAddress(request.getAddress());
        hotel.setEmail(request.getEmail());
        hotel.setPhone(request.getPhone());
        hotel.setIsActive(true);

        Plan plan = planRepository.findById(request.getPlanId())
                .orElseThrow(() -> new RuntimeException("Plan not found with ID: " + request.getPlanId()));
        hotel.setPlan(plan);

        Hotel savedHotel = hotelRepository.save(hotel);
        log.info("Hotel created successfully with ID={}", savedHotel.getId());
        return savedHotel;
    }

    public Optional<Hotel> updateHotel(UUID id, HotelRequest request) {
        return hotelRepository.findById(id).map(hotel -> {
            hotel.setName(request.getName());
            hotel.setAddress(request.getAddress());
            hotel.setEmail(request.getEmail());
            hotel.setPhone(request.getPhone());

            if (request.getPlanId() != null) {
                Plan plan = planRepository.findById(request.getPlanId())
                        .orElseThrow(() -> new RuntimeException("Plan not found"));
                hotel.setPlan(plan);
            }

            return hotelRepository.save(hotel);
        });
    }

    public void deleteHotel(UUID id) {
        hotelRepository.deleteById(id);
    }

    public HotelDTO convertToDTO(Hotel hotel) {
        if (hotel == null) {
            log.warn("convertToDTO called with null hotel");
            return null;
        }
        try {
            HotelDTO dto = new HotelDTO();
            dto.setId(hotel.getId());
            dto.setName(hotel.getName() != null ? hotel.getName() : "");
            dto.setAddress(hotel.getAddress());
            dto.setEmail(hotel.getEmail());
            dto.setPhone(hotel.getPhone());

            // Gérer le plan de manière sécurisée
            if (hotel.getPlan() != null) {
                dto.setPlanId(hotel.getPlan().getId());
                try {
                    // Utiliser getNameSafe() pour éviter les erreurs avec les plans invalides
                    SubscriptionPlan planName = hotel.getPlan().getNameSafe();
                    dto.setPlanName(planName != null ? planName.name() : null);
                } catch (Exception e) {
                    log.warn("Error getting plan name for hotel {}: {}", hotel.getId(), e.getMessage());
                    dto.setPlanName("STARTER"); // Valeur par défaut
                }
            } else {
                dto.setPlanId(null);
                dto.setPlanName(null);
            }

            dto.setIsActive(hotel.getIsActive() != null ? hotel.getIsActive() : true);
            return dto;
        } catch (Exception e) {
            log.error("Error converting hotel to DTO: hotelId={}, error={}",
                    hotel.getId() != null ? hotel.getId().toString() : "null", e.getMessage(), e);
            // Retourner null au lieu de lancer une exception pour éviter de bloquer toute
            // la liste
            return null;
        }
    }
}
