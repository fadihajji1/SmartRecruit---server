package com.smartrecruit.candidateai.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.smartrecruit.candidateai.domain.CandidateCv;
import com.smartrecruit.candidateai.domain.CvStatus;

public interface CandidateCvRepository extends JpaRepository<CandidateCv, UUID> {
    List<CandidateCv> findAllByCandidateIdAndStatusOrderByUploadedAtDesc(UUID candidateId, CvStatus status);
    Optional<CandidateCv> findByIdAndCandidateIdAndStatus(UUID id, UUID candidateId, CvStatus status);
    boolean existsByCandidateIdAndSha256AndStatus(UUID candidateId, String sha256, CvStatus status);
}
