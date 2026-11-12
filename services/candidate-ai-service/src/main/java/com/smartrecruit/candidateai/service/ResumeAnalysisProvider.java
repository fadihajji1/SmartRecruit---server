package com.smartrecruit.candidateai.service;

public interface ResumeAnalysisProvider {
    String providerName();
    ResumeAnalysis analyze(String resumeText, String jobDescription);
}
