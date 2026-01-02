package com.hotel.tickethub.repository;

import com.hotel.tickethub.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    // Récupérer les utilisateurs d'un hôtel avec leurs rôles chargés
    @Query("SELECT DISTINCT u FROM User u LEFT JOIN FETCH u.userRoles ur WHERE u.hotel.id = :hotelId")
    List<User> findByHotelIdWithRoles(@Param("hotelId") UUID hotelId);

    // Alternative : récupérer directement les utilisateurs d'un hôtel
    List<User> findByHotelId(UUID hotelId);
    
    // Récupérer les utilisateurs sans hôtel (hotel_id = NULL)
    // Utilisé pour les techniciens qui travaillent pour tous les hôtels
    List<User> findByHotelIdIsNull();
}
