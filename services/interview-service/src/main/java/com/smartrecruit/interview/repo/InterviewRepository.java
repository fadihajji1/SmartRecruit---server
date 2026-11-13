package com.smartrecruit.interview.repo;

import com.smartrecruit.interview.domain.Interview;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InterviewRepository extends JpaRepository<Interview, UUID> {
    List<Interview> findByRecruiterIdOrderByScheduledAtAsc(UUID recruiterId);
    Optional<Interview> findByIdAndRecruiterId(UUID id, UUID recruiterId);
    List<Interview> findByCandidateIdOrderByScheduledAtAsc(UUID candidateId);
}
