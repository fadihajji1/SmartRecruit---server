package com.smartrecruit.recruitment.repository;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.smartrecruit.recruitment.model.Application;

public interface ApplicationRepository extends JpaRepository<Application, UUID> {
	boolean existsByJob_IdAndCandidateId(UUID jobId, UUID candidateId);

	Page<Application> findAllByCandidateId(UUID candidateId, Pageable pageable);

	Optional<Application> findByIdAndCandidateId(UUID id, UUID candidateId);

	Page<Application> findAllByJob_Business_IdIn(Collection<UUID> businessIds, Pageable pageable);

	Optional<Application> findByIdAndJob_Business_IdIn(UUID id, Collection<UUID> businessIds);
}
