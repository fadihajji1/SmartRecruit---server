package com.smartrecruit.subscription.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.smartrecruit.subscription.domain.SubscriptionAudience;
import com.smartrecruit.subscription.domain.SubscriptionPlan;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, UUID> {
    Optional<SubscriptionPlan> findByPlanCodeIgnoreCase(String planCode);
    List<SubscriptionPlan> findAllByActiveTrueOrderByPriceAsc();
    List<SubscriptionPlan> findAllByAudienceAndActiveTrueOrderByPriceAsc(SubscriptionAudience audience);
    List<SubscriptionPlan> findAllByOrderByAudienceAscPriceAsc();
}
