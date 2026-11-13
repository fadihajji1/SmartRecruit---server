package com.smartrecruit.recruitment.repo;

import com.smartrecruit.recruitment.domain.Job;
import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface JobRepository extends JpaRepository<Job, UUID> {
    // Patterns are always non-null ("%" when no filter) so PostgreSQL never has to infer the type of a null parameter.
    @Query("""
        select j from Job j
        where j.status = :status
          and (lower(j.title) like :query or lower(coalesce(j.description, '')) like :query)
          and lower(coalesce(j.location, '')) like :location
          and lower(coalesce(j.contractType, '')) like :contractType
        """)
    Page<Job> searchPublished(@Param("status") Job.Status status, @Param("query") String query,
                              @Param("location") String location, @Param("contractType") String contractType,
                              Pageable pageable);
    List<Job> findByCreatedByUserId(UUID id);
    List<Job> findByBusinessIdIn(Collection<UUID> ids);
    List<Job> findByStatusAndExpiresAtBefore(Job.Status status, Instant expiresAt);
}
