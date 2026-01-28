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
        return hotelRepository.findAll().stream()
                .map(this::convertToDTO)
                .toList();
    }

    public Optional<Hotel> getHotelById(UUID id) {
        return hotelRepository.findById(id);
    }

    public Hotel createHotel(HotelRequest request) {
        log.debug("createHotel: request={}", request);
        
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new RuntimeException("Le nom de l'hôtel est requis");
        }
        
        if (request.getPlanId() == null) {
            throw new RuntimeException("Le plan d'abonnement est requis");
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
        HotelDTO dto = new HotelDTO();
        dto.setId(hotel.getId());
        dto.setName(hotel.getName());
        dto.setAddress(hotel.getAddress());
        dto.setEmail(hotel.getEmail());
        dto.setPhone(hotel.getPhone());
        dto.setPlanId(hotel.getPlan() != null ? hotel.getPlan().getId() : null);
        if (hotel.getPlan() != null && hotel.getPlan().getName() != null) {
            dto.setPlanName(hotel.getPlan().getName().name());
        }
        dto.setIsActive(hotel.getIsActive());
        return dto;
    }
}
