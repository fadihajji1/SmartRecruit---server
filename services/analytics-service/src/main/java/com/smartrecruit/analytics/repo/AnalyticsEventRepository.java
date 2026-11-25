package com.smartrecruit.analytics.repo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.smartrecruit.analytics.domain.AnalyticsEvent;

public interface AnalyticsEventRepository extends JpaRepository<AnalyticsEvent,UUID> {
    boolean existsByEventId(UUID eventId);
    List<AnalyticsEvent> findAllByRecruiterIdOrderByOccurredAtDesc(UUID recruiterId);
    Optional<AnalyticsEvent> findFirstByJobIdAndRecruiterIdOrderByOccurredAtDesc(UUID jobId,UUID recruiterId);
    Optional<AnalyticsEvent> findFirstByJobIdOrderByOccurredAtDesc(UUID jobId);
}
