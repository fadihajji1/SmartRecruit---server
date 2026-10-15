package com.smartrecruit.candidateai.repo;

import com.smartrecruit.candidateai.domain.CandidateCv;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface CandidateCvRepository extends JpaRepository<CandidateCv,UUID>{
List<CandidateCv> findByCandidateIdAndDeletedAtIsNullOrderByCreatedAtDesc(UUID candidateId);
Optional<CandidateCv> findByIdAndCandidateIdAndDeletedAtIsNull(UUID id,UUID candidateId);
}

