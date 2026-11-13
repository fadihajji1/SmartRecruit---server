package com.smartrecruit.interview.api;

import com.smartrecruit.interview.domain.Interview;
import com.smartrecruit.interview.events.InterviewEvents;
import com.smartrecruit.interview.repo.InterviewRepository;
import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class InterviewController {
    public record InterviewRequest(UUID applicationId, UUID candidateId, UUID jobId, String jobTitle, String candidateName,
                                   Instant scheduledAt, Integer durationMinutes, String externalMeetingUrl, String notes,
                                   Interview.Status status) {}

    private final InterviewRepository repo;
    private final InterviewEvents events;
    public InterviewController(InterviewRepository repo, InterviewEvents events) { this.repo = repo; this.events = events; }

    @GetMapping("/recruiter/interviews") @PreAuthorize("hasRole('RECRUITER')")
    public List<Interview> recruiterInterviews(@AuthenticationPrincipal Jwt jwt) { return repo.findByRecruiterIdOrderByScheduledAtAsc(uid(jwt)); }

    @GetMapping("/recruiter/interviews/{id}") @PreAuthorize("hasRole('RECRUITER')")
    public Interview recruiterInterview(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) { return owned(id, uid(jwt)); }

    @PostMapping("/recruiter/interviews") @PreAuthorize("hasRole('RECRUITER')") @ResponseStatus(HttpStatus.CREATED) @Transactional
    public Interview create(@RequestBody InterviewRequest r, @AuthenticationPrincipal Jwt jwt) {
        if (r.applicationId() == null || r.candidateId() == null) throw new IllegalArgumentException("Choose the application to interview");
        if (r.scheduledAt() == null || !r.scheduledAt().isAfter(Instant.now())) throw new IllegalArgumentException("Interview date must be in the future");
        Interview i = new Interview();
        i.applicationId = r.applicationId(); i.candidateId = r.candidateId(); i.jobId = r.jobId();
        i.jobTitle = trim(r.jobTitle(), 200); i.candidateName = trim(r.candidateName(), 200);
        i.scheduledAt = r.scheduledAt(); i.durationMinutes = duration(r.durationMinutes());
        i.externalMeetingUrl = meetingUrl(r.externalMeetingUrl()); i.notes = r.notes();
        i.recruiterId = uid(jwt); i.status = Interview.Status.SCHEDULED;
        Interview saved = repo.save(i);
        events.publish("INTERVIEW_SCHEDULED", saved);
        return saved;
    }

    @PatchMapping("/recruiter/interviews/{id}") @PreAuthorize("hasRole('RECRUITER')") @Transactional
    public Interview update(@PathVariable UUID id, @RequestBody InterviewRequest r, @AuthenticationPrincipal Jwt jwt) {
        Interview i = owned(id, uid(jwt));
        if (r.scheduledAt() != null) {
            if (!r.scheduledAt().isAfter(Instant.now())) throw new IllegalArgumentException("Interview date must be in the future");
            i.scheduledAt = r.scheduledAt();
        }
        if (r.durationMinutes() != null) i.durationMinutes = duration(r.durationMinutes());
        if (r.externalMeetingUrl() != null) i.externalMeetingUrl = meetingUrl(r.externalMeetingUrl());
        if (r.notes() != null) i.notes = r.notes();
        if (r.status() != null) i.status = r.status();
        Interview saved = repo.save(i);
        events.publish(saved.status == Interview.Status.CANCELLED ? "INTERVIEW_CANCELLED" : "INTERVIEW_UPDATED", saved);
        return saved;
    }

    @DeleteMapping("/recruiter/interviews/{id}") @PreAuthorize("hasRole('RECRUITER')") @ResponseStatus(HttpStatus.NO_CONTENT) @Transactional
    public void delete(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        Interview i = owned(id, uid(jwt));
        repo.delete(i);
        i.status = Interview.Status.CANCELLED;
        events.publish("INTERVIEW_CANCELLED", i);
    }

    @GetMapping("/candidate/interviews") @PreAuthorize("hasRole('CANDIDATE')")
    public List<Interview> candidateInterviews(@AuthenticationPrincipal Jwt jwt) { return repo.findByCandidateIdOrderByScheduledAtAsc(uid(jwt)); }

    private Interview owned(UUID id, UUID recruiter) { return repo.findByIdAndRecruiterId(id, recruiter).orElseThrow(NoSuchElementException::new); }
    private static UUID uid(Jwt jwt) {
        try { return UUID.fromString(jwt.getClaimAsString("user_id")); }
        catch (RuntimeException e) { throw new SecurityException("Token has no valid user_id"); }
    }
    private static int duration(Integer value) { return value == null ? 45 : Math.max(15, Math.min(value, 480)); }
    private static String trim(String v, int max) { return v == null ? null : v.length() > max ? v.substring(0, max) : v; }
    private static String meetingUrl(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            URI uri = URI.create(value.trim());
            if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null) throw new IllegalArgumentException();
            return uri.toString();
        } catch (IllegalArgumentException e) { throw new IllegalArgumentException("Meeting link must start with https://"); }
    }
}
