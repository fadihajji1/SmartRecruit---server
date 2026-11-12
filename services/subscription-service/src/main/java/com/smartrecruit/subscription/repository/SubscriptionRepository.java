package com.smartrecruit.subscription.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.smartrecruit.subscription.domain.Subscription;
import com.smartrecruit.subscription.domain.SubscriptionAudience;
import com.smartrecruit.subscription.domain.SubscriptionStatus;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
    Optional<Subscription> findFirstByOwnerIdAndAudienceAndStatusInOrderByCreatedAtDesc(UUID ownerId,
            SubscriptionAudience audience, List<SubscriptionStatus> statuses);
    Page<Subscription> findAllByAudienceOrderByCreatedAtDesc(SubscriptionAudience audience, Pageable pageable);
    Page<Subscription> findAllByOwnerIdOrderByCreatedAtDesc(UUID ownerId, Pageable pageable);
}
