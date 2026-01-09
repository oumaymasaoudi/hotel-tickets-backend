package com.hotel.tickethub.repository;

import com.hotel.tickethub.model.DataDeletionRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DataDeletionRequestRepository extends JpaRepository<DataDeletionRequest, UUID> {
    
    List<DataDeletionRequest> findByUserId(UUID userId);
    
    List<DataDeletionRequest> findByStatus(DataDeletionRequest.DeletionStatus status);
}

