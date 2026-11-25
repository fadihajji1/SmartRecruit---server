package com.smartrecruit.candidateai.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record RecommendationResponse(UUID jobId, String title, String location, BigDecimal score, String reason) {}
