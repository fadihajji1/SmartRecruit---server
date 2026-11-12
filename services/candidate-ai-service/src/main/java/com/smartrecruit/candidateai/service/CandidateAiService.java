package com.smartrecruit.candidateai.service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.apache.tika.Tika;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smartrecruit.candidateai.domain.CandidateCv;
import com.smartrecruit.candidateai.domain.CvAnalysis;
import com.smartrecruit.candidateai.domain.CvStatus;
import com.smartrecruit.candidateai.domain.JobProjection;
import com.smartrecruit.candidateai.dto.CandidateCvResponse;
import com.smartrecruit.candidateai.dto.CvAnalysisRequest;
import com.smartrecruit.candidateai.dto.CvAnalysisResponse;
import com.smartrecruit.candidateai.dto.RecommendationResponse;
import com.smartrecruit.candidateai.repository.CandidateCvRepository;
import com.smartrecruit.candidateai.repository.CvAnalysisRepository;
import com.smartrecruit.candidateai.repository.JobProjectionRepository;

@Service
public class CandidateAiService {
    private static final long MAX_CV_BYTES = 10L * 1024L * 1024L;
    private final CandidateCvRepository cvs;
    private final CvAnalysisRepository analyses;
    private final JobProjectionRepository jobs;
    private final CvObjectStorage storage;
    private final ResumeAnalysisProvider provider;
    private final ObjectMapper mapper;
    private final Tika tika = new Tika();

    public CandidateAiService(CandidateCvRepository cvs, CvAnalysisRepository analyses, JobProjectionRepository jobs,
            CvObjectStorage storage, ResumeAnalysisProvider provider, ObjectMapper mapper) {
        this.cvs = cvs; this.analyses = analyses; this.jobs = jobs; this.storage = storage;
        this.provider = provider; this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<CandidateCvResponse> list(UUID candidateId) {
        return cvs.findAllByCandidateIdAndStatusOrderByUploadedAtDesc(candidateId, CvStatus.ACTIVE)
            .stream().map(CandidateCvResponse::from).toList();
    }

    @Transactional
    public CandidateCvResponse upload(UUID candidateId, MultipartFile file) {
        if (file == null || file.isEmpty()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose a CV file");
        if (file.getSize() > MAX_CV_BYTES) throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "CV files must be 10 MB or smaller");
        String filename = safeFilename(file.getOriginalFilename());
        byte[] contents;
        try { contents = file.getBytes(); }
        catch (IOException ex) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CV could not be read", ex); }
        String contentType = validateFile(filename, contents);
        String digest = sha256(contents);
        if (cvs.existsByCandidateIdAndSha256AndStatus(candidateId, digest, CvStatus.ACTIVE)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "This CV has already been uploaded");
        }
        String objectKey = "cvs/" + candidateId + "/" + UUID.randomUUID();
        storage.put(objectKey, contents, contentType);
        try {
            CandidateCv saved = cvs.saveAndFlush(new CandidateCv(candidateId, filename, objectKey, contentType, contents.length, digest));
            return CandidateCvResponse.from(saved);
        } catch (RuntimeException ex) {
            try { storage.delete(objectKey); } catch (RuntimeException ignored) { }
            throw ex;
        }
    }

    @Transactional(readOnly = true)
    public CandidateCvResponse get(UUID candidateId, UUID cvId) { return CandidateCvResponse.from(requireCv(candidateId, cvId)); }

    @Transactional(readOnly = true)
    public DownloadedCv download(UUID candidateId, UUID cvId) {
        CandidateCv cv = requireCv(candidateId, cvId);
        return new DownloadedCv(cv.getOriginalFilename(), cv.getContentType(), storage.get(cv.getObjectKey()));
    }

    @Transactional
    public void delete(UUID candidateId, UUID cvId) {
        CandidateCv cv = requireCv(candidateId, cvId);
        storage.delete(cv.getObjectKey());
        cv.markDeleted();
        cvs.save(cv);
    }

    @Transactional
    public CvAnalysisResponse scan(UUID candidateId, UUID cvId, CvAnalysisRequest request) {
        CandidateCv cv = requireCv(candidateId, cvId);
        String text;
        try { text = tika.parseToString(new ByteArrayInputStream(storage.get(cv.getObjectKey()))); }
        catch (Exception ex) { throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "The CV text could not be extracted", ex); }
        if (text == null || text.isBlank()) throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "No readable text was found in this CV");
        if (text.length() > 500_000) text = text.substring(0, 500_000);
        ResumeAnalysis result = provider.analyze(text, request == null ? null : request.jobDescription());
        CvAnalysis analysis = analyses.save(new CvAnalysis(cvId, provider.providerName(), result.score(),
            json(result.skills()), json(result.matchedKeywords()), json(result.missingKeywords()), json(result.suggestions())));
        return response(analysis);
    }

    @Transactional(readOnly = true)
    public List<RecommendationResponse> recommendations(UUID candidateId) {
        List<CandidateCv> candidateCvs = cvs.findAllByCandidateIdAndStatusOrderByUploadedAtDesc(candidateId, CvStatus.ACTIVE);
        CvAnalysis latest = candidateCvs.stream().map(cv -> analyses.findFirstByCvIdOrderByCreatedAtDesc(cv.getId()).orElse(null))
            .filter(java.util.Objects::nonNull).max(java.util.Comparator.comparing(CvAnalysis::getCreatedAt)).orElse(null);
        if (latest == null) return List.of();
        List<String> skills = readList(latest.getSkillsJson());
        if (skills.isEmpty()) return List.of();
        return jobs.findAllByStatusIgnoreCaseOrderByUpdatedAtDesc("PUBLISHED").stream()
            .map(job -> recommendation(job, skills)).filter(item -> item.score().signum() > 0)
            .sorted(java.util.Comparator.comparing(RecommendationResponse::score).reversed()).limit(10).toList();
    }

    @Transactional
    public void applyJobEvent(String eventType, UUID jobId, JsonNode data, Instant occurredAt) {
        if (jobId == null) return;
        String title = text(data, "title", "jobTitle", "name");
        String description = text(data, "description", "summary");
        String location = text(data, "location", "city");
        UUID businessId = uuid(data, "businessId", "companyId");
        UUID recruiterId = uuid(data, "recruiterId", "createdByUserId", "ownerUserId");
        String status = text(data, "status");
        if (status == null) {
            String upper = eventType.toUpperCase(Locale.ROOT);
            status = upper.contains("PUBLISH") ? "PUBLISHED" : upper.contains("CLOSE") ? "CLOSED" : upper.contains("DELETE") ? "DELETED" : "DRAFT";
        }
        JobProjection existing = jobs.findById(jobId).orElse(null);
        if (existing == null) {
            if (title == null) return;
            jobs.save(new JobProjection(jobId, businessId, recruiterId, title,
                description == null ? "" : description, location, status.toUpperCase(Locale.ROOT), occurredAt == null ? Instant.now() : occurredAt));
        } else {
            existing.update(businessId, recruiterId, title, description, location, status.toUpperCase(Locale.ROOT), occurredAt == null ? Instant.now() : occurredAt);
            jobs.save(existing);
        }
    }

    private CandidateCv requireCv(UUID candidateId, UUID cvId) {
        return cvs.findByIdAndCandidateIdAndStatus(cvId, candidateId, CvStatus.ACTIVE)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "CV was not found"));
    }

    private CvAnalysisResponse response(CvAnalysis analysis) {
        return CvAnalysisResponse.from(analysis, readList(analysis.getSkillsJson()), readList(analysis.getMatchedKeywordsJson()),
            readList(analysis.getMissingKeywordsJson()), readList(analysis.getSuggestionsJson()));
    }

    private RecommendationResponse recommendation(JobProjection job, List<String> skills) {
        String text = (job.getTitle() + " " + job.getDescription()).toLowerCase(Locale.ROOT);
        List<String> matches = skills.stream().filter(skill -> text.contains(skill.toLowerCase(Locale.ROOT))).toList();
        if (matches.isEmpty()) return new RecommendationResponse(job.getJobId(), job.getTitle(), job.getLocation(), java.math.BigDecimal.ZERO, "No extracted skill terms matched");
        java.math.BigDecimal score = java.math.BigDecimal.valueOf(100.0 * matches.size() / Math.max(1, skills.size())).setScale(2, java.math.RoundingMode.HALF_UP);
        return new RecommendationResponse(job.getJobId(), job.getTitle(), job.getLocation(), score,
            "Matches " + String.join(", ", matches) + " from your latest CV analysis");
    }

    private String validateFile(String filename, byte[] data) {
        String extension = filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
        boolean pdf = extension.equals("pdf") && startsWith(data, new byte[]{'%', 'P', 'D', 'F', '-'});
        boolean doc = extension.equals("doc") && startsWith(data, new byte[]{(byte) 0xD0, (byte) 0xCF, 0x11, (byte) 0xE0, (byte) 0xA1, (byte) 0xB1, 0x1A, (byte) 0xE1});
        boolean docx = extension.equals("docx") && startsWith(data, new byte[]{'P', 'K', 3, 4});
        if (pdf) return "application/pdf";
        if (doc) return "application/msword";
        if (docx) return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Only valid PDF, DOC, and DOCX CV files are accepted");
    }

    private static boolean startsWith(byte[] contents, byte[] signature) {
        if (contents.length < signature.length) return false;
        for (int i = 0; i < signature.length; i++) if (contents[i] != signature[i]) return false;
        return true;
    }

    private static String safeFilename(String original) {
        if (original == null || original.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CV filename is required");
        String name = original.replace('\\', '/');
        name = name.substring(name.lastIndexOf('/') + 1).replaceAll("[\\r\\n\\p{Cntrl}]", "_").trim();
        if (name.length() > 255) name = name.substring(name.length() - 255);
        if (!name.contains(".")) throw new ResponseStatusException(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "CV file extension is not supported");
        return name;
    }

    private static String sha256(byte[] data) {
        try { return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(data)); }
        catch (Exception ex) { throw new IllegalStateException("SHA-256 is unavailable", ex); }
    }

    private String json(List<String> values) {
        try { return mapper.writeValueAsString(values); }
        catch (JsonProcessingException ex) { throw new IllegalStateException("Could not persist analysis result", ex); }
    }

    private List<String> readList(String value) {
        try { return mapper.readValue(value, new TypeReference<>() {}); }
        catch (Exception ex) { return List.of(); }
    }

    private static String text(JsonNode node, String... names) {
        for (String name : names) if (node.hasNonNull(name) && !node.get(name).asText().isBlank()) return node.get(name).asText();
        return null;
    }
    private static UUID uuid(JsonNode node, String... names) {
        String value = text(node, names);
        try { return value == null ? null : UUID.fromString(value); }
        catch (IllegalArgumentException ex) { return null; }
    }

    public record DownloadedCv(String filename, String contentType, byte[] contents) {}
}
