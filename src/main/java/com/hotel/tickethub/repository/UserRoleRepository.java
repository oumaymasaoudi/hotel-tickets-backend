package com.hotel.tickethub.repository;

import com.hotel.tickethub.model.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRoleRepository extends JpaRepository<UserRole, UUID> {

    // Standard method - works when user_id is UUID
    Optional<UserRole> findByUserId(UUID userId);

    // Custom query using JPQL - Hibernate will handle the type conversion
    // This uses the @ManyToOne relationship which should work even with type
    // differences
    @Query("SELECT ur FROM UserRole ur JOIN ur.user u WHERE u.id = :userId")
    Optional<UserRole> findByUserIdCustom(@Param("userId") UUID userId);

    // Alternative: Query using native SQL with explicit conversion
    // Note: This requires the result to be mapped correctly
    @Query(value = "SELECT ur.* FROM user_roles ur WHERE ur.user_id = CAST(:userId AS uuid)", nativeQuery = true)
    Optional<UserRole> findByUserIdNative(@Param("userId") UUID userId);

    List<UserRole> findByHotelId(UUID hotelId);
}
