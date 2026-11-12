package com.smartrecruit.interview.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartrecruit.interview.model.DelayRequest;
import com.smartrecruit.interview.model.DelayRequestStatus;

public interface DelayRequestRepository extends JpaRepository<DelayRequest, UUID> {
    Optional<DelayRequest> findByIdAndInterviewRecruiterId(UUID id, UUID recruiterId);
    Optional<DelayRequest> findFirstByInterviewIdAndInterviewRecruiterIdAndStatusOrderByCreatedAtDesc(
            UUID interviewId, UUID recruiterId, DelayRequestStatus status);
    boolean existsByInterviewIdAndRequestedByUserIdAndStatus(UUID interviewId, UUID userId, DelayRequestStatus status);
}
