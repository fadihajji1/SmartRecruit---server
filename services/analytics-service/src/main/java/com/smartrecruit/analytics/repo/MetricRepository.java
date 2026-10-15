package com.smartrecruit.analytics.repo;

import com.smartrecruit.analytics.domain.Metric;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
public interface MetricRepository extends JpaRepository<Metric,UUID>{
}

