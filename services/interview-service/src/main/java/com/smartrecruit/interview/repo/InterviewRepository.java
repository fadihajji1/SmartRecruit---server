package com.smartrecruit.interview.repo;

import com.smartrecruit.interview.domain.Interview;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
public interface InterviewRepository extends JpaRepository<Interview,UUID>{
 java.util.List<Interview> findByRecruiterId(UUID recruiterId);
 java.util.Optional<Interview> findByIdAndRecruiterId(UUID id,UUID recruiterId);
 java.util.List<Interview> findByCandidateId(UUID candidateId);
 java.util.Optional<Interview> findByIdAndCandidateId(UUID id,UUID candidateId);
}

