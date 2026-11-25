package com.smartrecruit.candidateai.dto;

import jakarta.validation.constraints.Size;

public record CvAnalysisRequest(@Size(max = 20_000) String jobDescription) {}
