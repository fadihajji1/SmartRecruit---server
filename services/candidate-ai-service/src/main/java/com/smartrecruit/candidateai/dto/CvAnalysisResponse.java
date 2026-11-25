package com.smartrecruit.candidateai.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import com.smartrecruit.candidateai.domain.CvAnalysis;

public record CvAnalysisResponse(UUID id, UUID cvId, String analysisType, String provider, String model,
        BigDecimal score, List<String> skills, List<String> matchedKeywords, List<String> missingKeywords,
        List<String> suggestions, Instant createdAt) {
    public static CvAnalysisResponse from(CvAnalysis a, List<String> skills, List<String> matched,
            List<String> missing, List<String> suggestions) {
        return new CvAnalysisResponse(a.getId(), a.getCvId(), a.getAnalysisType(), a.getProvider(), a.getModel(),
            a.getScore(), skills, matched, missing, suggestions, a.getCreatedAt());
    }
}
