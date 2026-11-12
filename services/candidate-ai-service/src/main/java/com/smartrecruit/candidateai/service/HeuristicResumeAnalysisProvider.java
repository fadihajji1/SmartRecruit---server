package com.smartrecruit.candidateai.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class HeuristicResumeAnalysisProvider implements ResumeAnalysisProvider {
    private static final List<String> SKILL_CATALOG = List.of("java", "spring", "spring boot", "typescript", "javascript", "angular", "react", "node.js", "python", "django", "sql", "postgresql", "mysql", "mongodb", "kafka", "docker", "kubernetes", "aws", "azure", "gcp", "git", "rest api", "graphql", "machine learning", "data analysis", "communication", "leadership", "project management", "agile", "scrum", "html", "css", "c++", "c#", "php", "linux");
    private static final Set<String> STOP_WORDS = Set.of("the", "and", "for", "with", "from", "this", "that", "you", "your", "are", "our", "will", "have", "has", "not", "but", "all", "can", "was", "were", "who", "what", "where", "when", "how", "into", "about", "team", "work", "years", "year", "job", "role", "must", "able", "including", "required", "requirements", "responsibilities", "experience", "skills", "using", "used", "new");

    @Override public String providerName() { return "heuristic"; }

    @Override
    public ResumeAnalysis analyze(String resumeText, String jobDescription) {
        String normalizedResume = normalize(resumeText);
        List<String> skills = SKILL_CATALOG.stream().filter(skill -> contains(normalizedResume, skill)).toList();
        List<String> targetKeywords = keywords(jobDescription);
        List<String> matched = targetKeywords.stream().filter(keyword -> contains(normalizedResume, keyword)).toList();
        List<String> missing = targetKeywords.stream().filter(keyword -> !contains(normalizedResume, keyword)).limit(12).toList();
        List<String> suggestions = new ArrayList<>();
        if (resumeText.length() < 900) suggestions.add("Add measurable achievements and more detail about your recent work.");
        if (!normalizedResume.contains("education")) suggestions.add("Add a clearly labeled education section.");
        if (!normalizedResume.contains("experience")) suggestions.add("Add a clearly labeled work experience section.");
        if (!normalizedResume.matches("(?s).*\\b[\\w.%+-]+@[\\w.-]+\\.[a-z]{2,}\\b.*")) suggestions.add("Include a professional email address in the contact section.");
        if (!missing.isEmpty()) suggestions.add("Where accurate, include relevant job terms: " + String.join(", ", missing.stream().limit(6).toList()) + ".");
        if (suggestions.isEmpty()) suggestions.add("Keep the resume focused on measurable outcomes and tailor the summary to the target role.");

        int sectionCount = 0;
        for (String section : List.of("experience", "education", "skills", "projects")) if (normalizedResume.contains(section)) sectionCount++;
        double lengthScore = Math.min(20, Math.max(0, resumeText.length() / 120.0));
        double structureScore = sectionCount * 10.0;
        double contactScore = normalizedResume.matches("(?s).*\\b[\\w.%+-]+@[\\w.-]+\\.[a-z]{2,}\\b.*") ? 10.0 : 0.0;
        double matchScore = targetKeywords.isEmpty() ? Math.min(20, skills.size() * 2.0)
            : 20.0 * matched.size() / targetKeywords.size();
        BigDecimal score = BigDecimal.valueOf(Math.min(100, lengthScore + structureScore + contactScore + matchScore))
            .setScale(2, RoundingMode.HALF_UP);
        return new ResumeAnalysis(score, skills, matched, missing, suggestions);
    }

    private static List<String> keywords(String text) {
        if (text == null || text.isBlank()) return List.of();
        LinkedHashSet<String> terms = new LinkedHashSet<>();
        Arrays.stream(normalize(text).split("[^a-z0-9+#.]+"))
            .filter(term -> term.length() >= 3 && !STOP_WORDS.contains(term))
            .limit(40).forEach(terms::add);
        return terms.stream().limit(20).toList();
    }

    private static boolean contains(String normalizedText, String term) {
        String escaped = Pattern.quote(normalize(term));
        return Pattern.compile("(?<![a-z0-9])" + escaped + "(?![a-z0-9])", Pattern.CASE_INSENSITIVE).matcher(normalizedText).find();
    }

    private static String normalize(String value) { return value == null ? "" : value.toLowerCase(Locale.ROOT).replaceAll("\\s+", " "); }
}
