package com.smartrecruit.recruitment.repo;
import com.smartrecruit.recruitment.domain.Job;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface JobRepository extends JpaRepository<Job,UUID>{
Page<Job> findByStatusAndTitleContainingIgnoreCase(Job.Status status,String query,Pageable pageable);
List<Job> findByCreatedByUserId(UUID id);
}

