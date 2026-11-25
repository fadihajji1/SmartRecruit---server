package com.smartrecruit.analytics.dto;

import java.time.Instant;
import java.util.Map;

public record AnalyticsOverviewResponse(long publishedJobs,long closedJobs,long applications,long interviews,
        long hires,Map<String,Long> applicationsByStatus,Instant generatedAt) {}
