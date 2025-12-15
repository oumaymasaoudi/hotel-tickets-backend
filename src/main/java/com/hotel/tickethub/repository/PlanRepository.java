package com.hotel.tickethub.repository;

import com.hotel.tickethub.model.Plan;
import com.hotel.tickethub.model.enums.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlanRepository extends JpaRepository<Plan, UUID> {
    Optional<Plan> findByName(SubscriptionPlan name);
}
