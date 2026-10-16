package com.smartrecruit.recruitment.repo;
import com.smartrecruit.recruitment.domain.Job;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
public interface JobRepository extends JpaRepository<Job,UUID>{
Page<Job> findByStatusAndTitleContainingIgnoreCase(Job.Status status,String query,Pageable pageable);
@Query("select j from Job j where j.status = :status and (:query is null or lower(j.title) like lower(concat('%', :query, '%')) or lower(j.description) like lower(concat('%', :query, '%'))) and (:location is null or lower(j.location) like lower(concat('%', :location, '%'))) and (:contractType is null or lower(j.contractType) like lower(concat('%', :contractType, '%')))")
Page<Job> searchPublished(@Param("status") Job.Status status,@Param("query") String query,@Param("location") String location,@Param("contractType") String contractType,Pageable pageable);
List<Job> findByCreatedByUserId(UUID id);
List<Job> findByBusinessIdIn(Collection<UUID> ids);
List<Job> findByStatusAndExpiresAtBefore(Job.Status status,java.time.Instant expiresAt);
}

