package com.smartrecruit.recruitment.repository;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smartrecruit.recruitment.model.JobOffer;
import com.smartrecruit.recruitment.model.JobStatus;

public interface JobOfferRepository extends JpaRepository<JobOffer, UUID> {
	@Query("""
			select job from JobOffer job
			where job.status = :status
			  and (:query is null or lower(job.title) like lower(concat('%', :query, '%'))
			       or lower(job.description) like lower(concat('%', :query, '%')))
			  and (:location is null or lower(coalesce(job.location, '')) like lower(concat('%', :location, '%')))
			  and (:contractType is null or job.contractType = :contractType)
			""")
	Page<JobOffer> searchPublicJobs(
			@Param("status") JobStatus status,
			@Param("query") String query,
			@Param("location") String location,
			@Param("contractType") String contractType,
			Pageable pageable);

	Page<JobOffer> findAllByBusiness_IdIn(Collection<UUID> businessIds, Pageable pageable);

	Optional<JobOffer> findByIdAndBusiness_IdIn(UUID id, Collection<UUID> businessIds);

	Optional<JobOffer> findByIdAndStatus(UUID id, JobStatus status);
}
