package com.smartrecruit.candidateai.service;

import java.math.BigDecimal;
import java.util.List;

public record ResumeAnalysis(BigDecimal score, List<String> skills, List<String> matchedKeywords,
        List<String> missingKeywords, List<String> suggestions) {}
