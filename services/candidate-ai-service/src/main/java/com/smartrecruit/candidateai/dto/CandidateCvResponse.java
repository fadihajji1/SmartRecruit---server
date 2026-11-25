package com.smartrecruit.candidateai.dto;

import java.time.Instant;
import java.util.UUID;
import com.smartrecruit.candidateai.domain.CandidateCv;
import com.smartrecruit.candidateai.domain.CvStatus;

public record CandidateCvResponse(UUID id, UUID candidateId, String originalFilename, String contentType,
        long sizeBytes, CvStatus status, Instant uploadedAt, Instant deletedAt) {
    public static CandidateCvResponse from(CandidateCv cv) {
        return new CandidateCvResponse(cv.getId(), cv.getCandidateId(), cv.getOriginalFilename(),
            cv.getContentType(), cv.getSizeBytes(), cv.getStatus(), cv.getUploadedAt(), cv.getDeletedAt());
    }
}
