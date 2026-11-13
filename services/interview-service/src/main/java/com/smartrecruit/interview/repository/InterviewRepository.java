package com.smartrecruit.interview.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartrecruit.interview.model.Interview;

public interface InterviewRepository extends JpaRepository<Interview, UUID> {
    List<Interview> findByRecruiterIdOrderByScheduledAtAsc(UUID recruiterId);
    List<Interview> findByCandidateIdOrderByScheduledAtAsc(UUID candidateId);
    Optional<Interview> findByIdAndRecruiterId(UUID id, UUID recruiterId);
    Optional<Interview> findByIdAndCandidateId(UUID id, UUID candidateId);
}
