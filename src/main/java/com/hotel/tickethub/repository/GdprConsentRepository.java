package com.hotel.tickethub.repository;

import com.hotel.tickethub.model.GdprConsent;
import com.hotel.tickethub.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GdprConsentRepository extends JpaRepository<GdprConsent, UUID> {
    
    List<GdprConsent> findByUser(User user);
    
    List<GdprConsent> findByUserId(UUID userId);
    
    Optional<GdprConsent> findByUserIdAndConsentType(UUID userId, String consentType);
    
    List<GdprConsent> findByUserIdAndConsented(UUID userId, Boolean consented);
}

