package com.smartrecruit.recruitment.service;

import com.smartrecruit.recruitment.domain.Application;
import com.smartrecruit.recruitment.domain.Business;
import com.smartrecruit.recruitment.domain.Job;
import com.smartrecruit.recruitment.events.OutboxPublisher;
import com.smartrecruit.recruitment.repo.ApplicationRepository;
import com.smartrecruit.recruitment.repo.BusinessRepository;
import com.smartrecruit.recruitment.repo.JobRepository;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecruitmentService {
    private final JobRepository jobs;
    private final ApplicationRepository applications;
    private final BusinessRepository businesses;
    private final OutboxPublisher outbox;

    public RecruitmentService(JobRepository jobs, ApplicationRepository applications, BusinessRepository businesses, OutboxPublisher outbox) {
        this.jobs = jobs; this.applications = applications; this.businesses = businesses; this.outbox = outbox;
    }

    // ---------- public catalogue ----------
    @Transactional(readOnly = true)
    public Page<Job> publicJobs(String query, String location, String contractType, int page, int size) {
        int safePage = Math.max(page, 0), safeSize = Math.min(Math.max(size, 1), 100);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by("createdAt").descending());
        Page<Job> result = jobs.searchPublished(Job.Status.PUBLISHED, pattern(query), pattern(location), pattern(contractType), pageable);
        withBusiness(result.getContent());
        return result;
    }

    private static String pattern(String value) {
        if (value == null || value.isBlank()) return "%";
        String escaped = value.trim().toLowerCase(Locale.ROOT);
        return "%" + escaped + "%";
    }

    @Transactional(readOnly = true)
    public Job publicJob(UUID id) {
        Job job = jobs.findById(id).orElseThrow();
        if (job.status != Job.Status.PUBLISHED) throw new NoSuchElementException();
        withBusiness(List.of(job));
        return job;
    }

    /** Copies public company details onto jobs for display; never persisted. */
    private void withBusiness(List<Job> list) {
        var ids = list.stream().map(j -> j.businessId).filter(java.util.Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) return;
        Map<UUID, Business> byId = new java.util.HashMap<>();
        businesses.findAllById(ids).forEach(b -> byId.put(b.id, b));
        for (Job j : list) {
            Business b = byId.get(j.businessId);
            if (b == null) continue;
            j.businessName = b.name; j.businessIndustry = b.industry; j.businessCity = b.city;
            j.businessCountry = b.country; j.businessWebsite = b.website; j.businessDescription = b.description;
        }
    }

    // ---------- businesses ----------
    @Transactional public Business createBusiness(Business b, UUID owner) { b.id = null; b.ownerUserId = owner; b.status = Business.Status.ACTIVE; return businesses.save(b); }
    @Transactional(readOnly = true) public List<Business> recruiterBusinesses(UUID owner) { return businesses.findByOwnerUserId(owner); }
    @Transactional(readOnly = true) public List<Business> allBusinesses() { return businesses.findAll(); }
    @Transactional(readOnly = true) public Business adminBusiness(UUID id) { return businesses.findById(id).orElseThrow(); }
    @Transactional public Business adminCreateBusiness(Business b) {
        b.id = null;
        if (b.ownerUserId == null) throw new IllegalArgumentException("ownerUserId is required");
        if (b.status == null) b.status = Business.Status.ACTIVE;
        return businesses.save(b);
    }
    @Transactional public void deactivateBusiness(UUID id) { businesses.findById(id).orElseThrow().status = Business.Status.INACTIVE; }
    @Transactional public void deactivateBusiness(UUID id, UUID owner) { business(id, owner).status = Business.Status.INACTIVE; }
    @Transactional(readOnly = true) public Business business(UUID id, UUID owner) {
        Business b = businesses.findById(id).orElseThrow();
        if (!b.ownerUserId.equals(owner)) throw new SecurityException("Business access denied");
        return b;
    }
    @Transactional public Business updateBusiness(UUID id, Business input, UUID owner) {
        Business b = business(id, owner);
        b.name = input.name; b.description = input.description; b.website = input.website; b.industry = input.industry;
        b.address = input.address; b.city = input.city; b.country = input.country;
        return b;
    }
    @Transactional public Business adminUpdateBusiness(UUID id, Business input) {
        Business b = businesses.findById(id).orElseThrow();
        if (input.name != null) b.name = input.name;
        if (input.description != null) b.description = input.description;
        if (input.website != null) b.website = input.website;
        if (input.industry != null) b.industry = input.industry;
        if (input.address != null) b.address = input.address;
        if (input.city != null) b.city = input.city;
        if (input.country != null) b.country = input.country;
        if (input.status != null) b.status = input.status;
        return b;
    }

    // ---------- jobs ----------
    @Transactional public Job createJob(Job job, UUID recruiterId) {
        job.id = null; job.createdByUserId = recruiterId;
        if (job.businessId == null) throw new IllegalArgumentException("Business is required");
        if (business(job.businessId, recruiterId).status != Business.Status.ACTIVE) throw new IllegalStateException("Business is inactive");
        if (job.expiresAt != null && !job.expiresAt.isAfter(Instant.now())) throw new IllegalArgumentException("Offer expiration must be in the future");
        job.status = Job.Status.DRAFT;
        Job saved = jobs.save(job);
        jobEvent("JOB_CREATED", saved);
        return saved;
    }
    @Transactional(readOnly = true) public List<Job> recruiterJobs(UUID recruiter) { return jobs.findByCreatedByUserId(recruiter); }
    @Transactional(readOnly = true) public Job recruiterJob(UUID id, UUID recruiter) { return ownedJob(id, recruiter); }
    @Transactional public Job updateJob(UUID id, Job input, UUID recruiterId) {
        Job job = ownedJob(id, recruiterId);
        if (job.status == Job.Status.DELETED) throw new IllegalStateException("Deleted jobs cannot be changed");
        if (input.businessId == null) throw new IllegalArgumentException("Business is required");
        if (business(input.businessId, recruiterId).status != Business.Status.ACTIVE) throw new IllegalStateException("Business is inactive");
        if (input.expiresAt != null && !input.expiresAt.isAfter(Instant.now())) throw new IllegalArgumentException("Offer expiration must be in the future");
        job.businessId = input.businessId; job.title = input.title; job.description = input.description;
        job.location = input.location; job.contractType = input.contractType; job.expiresAt = input.expiresAt;
        jobEvent("JOB_UPDATED", job);
        return job;
    }
    @Transactional public Job transition(UUID id, UUID recruiterId, Job.Status status) {
        Job job = ownedJob(id, recruiterId);
        boolean allowed = switch (status) {
            case PUBLISHED -> job.status == Job.Status.DRAFT || job.status == Job.Status.CLOSED;
            case CLOSED -> job.status == Job.Status.PUBLISHED;
            case DELETED -> job.status != Job.Status.DELETED;
            default -> false;
        };
        if (!allowed) throw new IllegalStateException("Invalid job status transition");
        if (status == Job.Status.PUBLISHED && job.expiresAt != null && !job.expiresAt.isAfter(Instant.now()))
            throw new IllegalStateException("Expired offers cannot be published");
        job.status = status;
        if (status == Job.Status.PUBLISHED) job.publishedAt = Instant.now();
        if (status == Job.Status.DELETED) applications.findByJobId(id).forEach(a -> a.status = Application.Status.UNAVAILABLE);
        jobEvent("JOB_" + status.name(), job);
        return job;
    }
    @Transactional public void expire(Job job) { job.status = Job.Status.EXPIRED; jobEvent("JOB_EXPIRED", job); }

    // ---------- platform administration ----------
    @Transactional(readOnly = true) public List<Job> allJobs() {
        return jobs.findAll(Sort.by("createdAt").descending()).stream().filter(j -> j.status != Job.Status.DELETED).toList();
    }
    @Transactional public Job adminClose(UUID id) {
        Job job = jobs.findById(id).orElseThrow();
        if (job.status != Job.Status.PUBLISHED) throw new IllegalStateException("Only published offers can be closed");
        job.status = Job.Status.CLOSED;
        jobEvent("JOB_CLOSED", job);
        return job;
    }
    @Transactional(readOnly = true) public Map<String, Long> adminStats() {
        Map<String, Long> stats = new LinkedHashMap<>();
        for (Job.Status s : Job.Status.values()) stats.put("jobs" + s.name(), 0L);
        jobs.findAll().forEach(j -> stats.merge("jobs" + j.status.name(), 1L, Long::sum));
        stats.put("applications", applications.count());
        stats.put("businesses", businesses.count());
        return stats;
    }

    // ---------- applications ----------
    @Transactional public Application apply(UUID jobId, UUID cvId, String message, UUID candidateId, String candidateName, String candidateEmail) {
        Job job = jobs.findById(jobId).orElseThrow();
        if (job.status != Job.Status.PUBLISHED) throw new IllegalStateException("Offer is not open");
        if (cvId == null) throw new IllegalArgumentException("Candidate CV is required");
        if (applications.existsByJobIdAndCandidateId(jobId, candidateId)) throw new IllegalStateException("Already applied");
        Application a = new Application();
        a.jobId = jobId; a.candidateId = candidateId; a.candidateCvId = cvId; a.status = Application.Status.PENDING; a.message = message;
        a.jobTitle = job.title; a.candidateName = candidateName; a.candidateEmail = candidateEmail;
        a = applications.save(a);
        Map<String, Object> payload = applicationPayload(a, job);
        payload.put("recipientUserIds", List.of(candidateId.toString(), job.createdByUserId.toString()));
        outbox.enqueue("APPLICATION_SUBMITTED", a.id, payload);
        return a;
    }
    @Transactional(readOnly = true) public List<Application> candidateApplications(UUID id) { return applications.findByCandidateId(id); }
    @Transactional(readOnly = true) public Application candidateApplication(UUID id, UUID candidate) { return applications.findByIdAndCandidateId(id, candidate).orElseThrow(); }
    @Transactional(readOnly = true) public List<Application> recruiterApplications(UUID recruiter) {
        return jobs.findByCreatedByUserId(recruiter).stream().flatMap(j -> applications.findByJobId(j.id).stream()).toList();
    }
    @Transactional(readOnly = true) public Application recruiterApplication(UUID id, UUID recruiter) {
        Application a = applications.findById(id).orElseThrow(); ownedJob(a.jobId, recruiter); return a;
    }
    @Transactional public Application updateStatus(UUID id, Application.Status status, UUID recruiter) {
        Application a = recruiterApplication(id, recruiter);
        if (status == Application.Status.UNAVAILABLE) throw new IllegalArgumentException("Unavailable is a system-managed status");
        Application.Status previous = a.status;
        a.status = status;
        if (previous != status) {
            Job job = jobs.findById(a.jobId).orElseThrow();
            Map<String, Object> payload = applicationPayload(a, job);
            payload.put("previousStatus", String.valueOf(previous));
            outbox.enqueue("APPLICATION_STATUS_CHANGED", a.id, payload);
        }
        return a;
    }

    private Job ownedJob(UUID id, UUID recruiter) {
        Job j = jobs.findById(id).orElseThrow();
        if (!recruiter.equals(j.createdByUserId)) throw new SecurityException("Job access denied");
        return j;
    }

    private void jobEvent(String type, Job job) {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("jobId", str(job.id));
        p.put("businessId", str(job.businessId));
        p.put("recruiterId", str(job.createdByUserId));
        p.put("title", job.title);
        p.put("description", job.description);
        p.put("location", job.location);
        p.put("contractType", job.contractType);
        p.put("status", job.status == null ? null : job.status.name());
        outbox.enqueue(type, job.id, p);
    }

    private static Map<String, Object> applicationPayload(Application a, Job job) {
        Map<String, Object> p = new LinkedHashMap<>();
        p.put("applicationId", str(a.id));
        p.put("jobId", str(job.id));
        p.put("jobTitle", job.title);
        p.put("businessId", str(job.businessId));
        p.put("recruiterId", str(job.createdByUserId));
        p.put("candidateId", str(a.candidateId));
        p.put("status", a.status == null ? null : a.status.name());
        return p;
    }

    private static String str(UUID id) { return id == null ? null : id.toString(); }
}
