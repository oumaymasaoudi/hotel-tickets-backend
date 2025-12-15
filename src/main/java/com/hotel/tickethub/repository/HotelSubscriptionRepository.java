package com.hotel.tickethub.repository;

import com.hotel.tickethub.model.HotelSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HotelSubscriptionRepository extends JpaRepository<HotelSubscription, UUID> {

    /**
     * Récupérer l'abonnement actuel d'un hôtel
     */
    Optional<HotelSubscription> findByHotelIdAndStatus(UUID hotelId, String status);

    /**
     * Récupérer tous les abonnements d'un hôtel
     */
    List<HotelSubscription> findByHotelIdOrderByStartDateDesc(UUID hotelId);

    /**
     * Récupérer l'abonnement courant avec catégories additionnelles
     */
    @Query("SELECT hs FROM HotelSubscription hs WHERE hs.hotel.id = :hotelId AND hs.status = 'ACTIVE'")
    Optional<HotelSubscription> findActiveSubscriptionByHotel(@Param("hotelId") UUID hotelId);

    /**
     * Récupérer les changements de plan en attente
     */
    @Query("SELECT hs FROM HotelSubscription hs WHERE hs.isPendingChange = true")
    List<HotelSubscription> findPendingPlanChanges();
}
