package com.smartrecruit.candidateai.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "cv_analysis")
public class CvAnalysis {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name = "cv_id", nullable = false) private UUID cvId;
    @Column(name = "analysis_type", nullable = false, length = 32) private String analysisType;
    @Column(nullable = false, length = 32) private String provider;
    @Column(length = 80) private String model;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 16) private AnalysisStatus status;
    @Column(precision = 5, scale = 2) private BigDecimal score;
    @Column(name = "skills_json", nullable = false, columnDefinition = "TEXT") private String skillsJson;
    @Column(name = "matched_keywords_json", nullable = false, columnDefinition = "TEXT") private String matchedKeywordsJson;
    @Column(name = "missing_keywords_json", nullable = false, columnDefinition = "TEXT") private String missingKeywordsJson;
    @Column(name = "suggestions_json", nullable = false, columnDefinition = "TEXT") private String suggestionsJson;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    protected CvAnalysis() {}
    public CvAnalysis(UUID cvId, String provider, BigDecimal score, String skillsJson,
            String matchedKeywordsJson, String missingKeywordsJson, String suggestionsJson) {
        this.cvId = cvId; this.analysisType = "ATS"; this.provider = provider; this.model = "resume-heuristics-v1";
        this.status = AnalysisStatus.COMPLETED; this.score = score; this.skillsJson = skillsJson;
        this.matchedKeywordsJson = matchedKeywordsJson; this.missingKeywordsJson = missingKeywordsJson;
        this.suggestionsJson = suggestionsJson; this.createdAt = Instant.now();
    }
    public UUID getId() { return id; }
    public UUID getCvId() { return cvId; }
    public String getAnalysisType() { return analysisType; }
    public String getProvider() { return provider; }
    public String getModel() { return model; }
    public AnalysisStatus getStatus() { return status; }
    public BigDecimal getScore() { return score; }
    public String getSkillsJson() { return skillsJson; }
    public String getMatchedKeywordsJson() { return matchedKeywordsJson; }
    public String getMissingKeywordsJson() { return missingKeywordsJson; }
    public String getSuggestionsJson() { return suggestionsJson; }
    public Instant getCreatedAt() { return createdAt; }
}
