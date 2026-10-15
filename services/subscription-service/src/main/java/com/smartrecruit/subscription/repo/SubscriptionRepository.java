package com.smartrecruit.subscription.repo;

import com.smartrecruit.subscription.domain.Subscription;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
public interface SubscriptionRepository extends JpaRepository<Subscription,UUID>{
}

