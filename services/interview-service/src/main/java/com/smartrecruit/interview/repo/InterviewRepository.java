package com.smartrecruit.interview.repo;

import com.smartrecruit.interview.domain.Interview;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
public interface InterviewRepository extends JpaRepository<Interview,UUID>{
}

