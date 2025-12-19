package com.hotel.tickethub.service;

import com.hotel.tickethub.dto.HotelDTO;
import com.hotel.tickethub.dto.HotelRequest;
import com.hotel.tickethub.model.Hotel;
import com.hotel.tickethub.model.Plan;
import com.hotel.tickethub.repository.HotelRepository;
import com.hotel.tickethub.repository.PlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HotelServiceTest {

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private PlanRepository planRepository;

    @InjectMocks
    private HotelService hotelService;

    private Hotel testHotel;
    private Plan testPlan;
    private UUID testHotelId;
    private UUID testPlanId;

    @BeforeEach
    void setUp() {
        testHotelId = UUID.randomUUID();
        testPlanId = UUID.randomUUID();

        testPlan = new Plan();
        testPlan.setId(testPlanId);
        testPlan.setName(com.hotel.tickethub.model.enums.SubscriptionPlan.BASIC);

        testHotel = new Hotel();
        testHotel.setId(testHotelId);
        testHotel.setName("Test Hotel");
        testHotel.setAddress("123 Test Street");
        testHotel.setEmail("test@hotel.com");
        testHotel.setPhone("1234567890");
        testHotel.setIsActive(true);
        testHotel.setPlan(testPlan);
    }

    @Test
    void testGetAllHotels_Success() {
        Hotel hotel2 = new Hotel();
        hotel2.setId(UUID.randomUUID());
        hotel2.setName("Hotel 2");

        when(hotelRepository.findAll()).thenReturn(Arrays.asList(testHotel, hotel2));

        List<Hotel> result = hotelService.getAllHotels();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Test Hotel", result.get(0).getName());
        verify(hotelRepository, times(1)).findAll();
    }

    @Test
    void testGetAllHotelsDTO_Success() {
        Hotel hotel2 = new Hotel();
        hotel2.setId(UUID.randomUUID());
        hotel2.setName("Hotel 2");

        when(hotelRepository.findAll()).thenReturn(Arrays.asList(testHotel, hotel2));

        List<HotelDTO> result = hotelService.getAllHotelsDTO();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Test Hotel", result.get(0).getName());
        verify(hotelRepository, times(1)).findAll();
    }

    @Test
    void testGetHotelById_Success() {
        when(hotelRepository.findById(testHotelId)).thenReturn(Optional.of(testHotel));

        Optional<Hotel> result = hotelService.getHotelById(testHotelId);

        assertTrue(result.isPresent());
        assertEquals("Test Hotel", result.get().getName());
        verify(hotelRepository, times(1)).findById(testHotelId);
    }

    @Test
    void testGetHotelById_NotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(hotelRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        Optional<Hotel> result = hotelService.getHotelById(nonExistentId);

        assertFalse(result.isPresent());
        verify(hotelRepository, times(1)).findById(nonExistentId);
    }

    @Test
    void testCreateHotel_Success() {
        HotelRequest request = new HotelRequest();
        request.setName("New Hotel");
        request.setAddress("456 New Street");
        request.setEmail("new@hotel.com");
        request.setPhone("9876543210");
        request.setPlanId(testPlanId);

        when(planRepository.findById(testPlanId)).thenReturn(Optional.of(testPlan));
        when(hotelRepository.save(any(Hotel.class))).thenAnswer(invocation -> {
            Hotel hotel = invocation.getArgument(0);
            hotel.setId(UUID.randomUUID());
            return hotel;
        });

        Hotel result = hotelService.createHotel(request);

        assertNotNull(result);
        assertEquals("New Hotel", result.getName());
        assertEquals("456 New Street", result.getAddress());
        assertEquals("new@hotel.com", result.getEmail());
        assertTrue(result.getIsActive());
        assertNotNull(result.getPlan());
        verify(planRepository, times(1)).findById(testPlanId);
        verify(hotelRepository, times(1)).save(any(Hotel.class));
    }

    @Test
    void testCreateHotel_WithoutPlan() {
        HotelRequest request = new HotelRequest();
        request.setName("New Hotel");
        request.setAddress("456 New Street");
        request.setEmail("new@hotel.com");
        request.setPhone("9876543210");
        request.setPlanId(null);

        when(hotelRepository.save(any(Hotel.class))).thenAnswer(invocation -> {
            Hotel hotel = invocation.getArgument(0);
            hotel.setId(UUID.randomUUID());
            return hotel;
        });

        Hotel result = hotelService.createHotel(request);

        assertNotNull(result);
        assertEquals("New Hotel", result.getName());
        assertNull(result.getPlan());
        verify(planRepository, never()).findById(any());
        verify(hotelRepository, times(1)).save(any(Hotel.class));
    }

    @Test
    void testCreateHotel_PlanNotFound() {
        HotelRequest request = new HotelRequest();
        request.setName("New Hotel");
        request.setPlanId(UUID.randomUUID());

        when(planRepository.findById(any(UUID.class))).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            hotelService.createHotel(request);
        });

        assertEquals("Plan not found", exception.getMessage());
        verify(planRepository, times(1)).findById(any(UUID.class));
        verify(hotelRepository, never()).save(any(Hotel.class));
    }
}
