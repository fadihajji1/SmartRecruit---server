package com.smartrecruit.candidateai.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import com.smartrecruit.candidateai.domain.JobProjection;

public interface JobProjectionRepository extends JpaRepository<JobProjection, UUID> {
    List<JobProjection> findAllByStatusIgnoreCaseOrderByUpdatedAtDesc(String status);
}
