package com.hotel.tickethub.service;

import com.hotel.tickethub.dto.HotelDTO;
import com.hotel.tickethub.dto.HotelRequest;
import com.hotel.tickethub.model.Hotel;
import com.hotel.tickethub.model.Plan;
import com.hotel.tickethub.repository.HotelRepository;
import com.hotel.tickethub.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class HotelService {

    private final HotelRepository hotelRepository;
    private final PlanRepository planRepository;

    public List<Hotel> getAllHotels() {
        return hotelRepository.findAll();
    }

    public List<HotelDTO> getAllHotelsDTO() {
        return hotelRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Optional<Hotel> getHotelById(UUID id) {
        return hotelRepository.findById(id);
    }

    public Hotel createHotel(HotelRequest request) {
        Hotel hotel = new Hotel();
        hotel.setName(request.getName());
        hotel.setAddress(request.getAddress());
        hotel.setEmail(request.getEmail());
        hotel.setPhone(request.getPhone());
        hotel.setIsActive(true);

        if (request.getPlanId() != null) {
            Plan plan = planRepository.findById(request.getPlanId())
                    .orElseThrow(() -> new RuntimeException("Plan not found"));
            hotel.setPlan(plan);
        }

        return hotelRepository.save(hotel);
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
