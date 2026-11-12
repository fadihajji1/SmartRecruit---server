package com.smartrecruit.analytics.dto;

import java.time.Instant;

public record ApplicationMetricResponse(String status,long count,Instant lastActivityAt) {}
