package com.smartrecruit.recruitment.repo;
import com.smartrecruit.recruitment.domain.Application;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ApplicationRepository extends JpaRepository<Application,UUID>{
boolean existsByJobIdAndCandidateId(UUID jobId,UUID candidateId);
List<Application> findByCandidateId(UUID candidateId);
List<Application> findByJobId(UUID jobId);
Optional<Application> findByIdAndCandidateId(UUID id, UUID candidateId);
}

