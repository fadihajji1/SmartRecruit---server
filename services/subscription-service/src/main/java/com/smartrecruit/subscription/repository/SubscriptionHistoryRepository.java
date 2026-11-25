package com.smartrecruit.subscription.repository;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.smartrecruit.subscription.domain.SubscriptionAudience;
import com.smartrecruit.subscription.domain.SubscriptionHistory;

public interface SubscriptionHistoryRepository extends JpaRepository<SubscriptionHistory, UUID> {
    Page<SubscriptionHistory> findAllBySubscriptionIdOrderByOccurredAtDesc(UUID subscriptionId, Pageable pageable);

    @Query(value = "select h from SubscriptionHistory h where h.subscriptionId in " +
            "(select s.id from Subscription s where s.ownerId = :ownerId and s.audience = :audience) " +
            "order by h.occurredAt desc",
        countQuery = "select count(h) from SubscriptionHistory h where h.subscriptionId in " +
            "(select s.id from Subscription s where s.ownerId = :ownerId and s.audience = :audience)")
    Page<SubscriptionHistory> findAllByOwnerAndAudience(@Param("ownerId") UUID ownerId,
            @Param("audience") SubscriptionAudience audience, Pageable pageable);
}
