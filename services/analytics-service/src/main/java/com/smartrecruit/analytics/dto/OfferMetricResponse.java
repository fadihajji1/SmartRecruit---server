package com.smartrecruit.analytics.dto;

import java.time.Instant;
import java.util.UUID;

public record OfferMetricResponse(UUID jobId,String title,String status,long applications,Instant lastActivityAt) {}
