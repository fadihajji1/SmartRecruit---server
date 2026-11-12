package com.smartrecruit.candidateai.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.smartrecruit.candidateai.domain.CvAnalysis;

public interface CvAnalysisRepository extends JpaRepository<CvAnalysis, UUID> {
    Optional<CvAnalysis> findFirstByCvIdOrderByCreatedAtDesc(UUID cvId);
}
