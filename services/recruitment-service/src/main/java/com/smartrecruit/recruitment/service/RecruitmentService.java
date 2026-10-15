package com.smartrecruit.recruitment.service;

import com.smartrecruit.recruitment.domain.Application;
import com.smartrecruit.recruitment.domain.Job;
import com.smartrecruit.recruitment.repo.ApplicationRepository;
import com.smartrecruit.recruitment.repo.JobRepository;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecruitmentService {
    private final JobRepository jobs;
    private final ApplicationRepository applications;

    public RecruitmentService(JobRepository jobs, ApplicationRepository applications) {
        this.jobs = jobs;
        this.applications = applications;
    }

    public Page<Job> publicJobs(String query, int page, int size) {
        return jobs.findByStatusAndTitleContainingIgnoreCase(Job.Status.PUBLISHED,
            query == null ? "" : query,
            PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100), Sort.by("createdAt").descending()));
    }

    public Job publicJob(UUID id) {
        Job job = jobs.findById(id).orElseThrow();
        if (job.status != Job.Status.PUBLISHED && job.status != Job.Status.CLOSED) throw new NoSuchElementException();
        return job;
    }

    @Transactional
    public Job createJob(Job job, UUID recruiterId) {
        job.id = null;
        job.createdByUserId = recruiterId;
        if (job.status == null) job.status = Job.Status.DRAFT;
        return jobs.save(job);
    }

    @Transactional
    public Job updateJob(UUID id, Job input, UUID recruiterId) {
        Job job = ownedJob(id, recruiterId);
        job.title = input.title;
        job.description = input.description;
        job.location = input.location;
        job.contractType = input.contractType;
        return job;
    }

    @Transactional
    public Job transition(UUID id, UUID recruiterId, Job.Status status) {
        Job job = ownedJob(id, recruiterId);
        job.status = status;
        if (status == Job.Status.DELETED) applications.findByJobId(id).forEach(a -> a.status = Application.Status.UNAVAILABLE);
        return job;
    }

    @Transactional
    public Application apply(UUID jobId, UUID cvId, UUID candidateId) {
        Job job = jobs.findById(jobId).orElseThrow();
        if (job.status != Job.Status.PUBLISHED) throw new IllegalStateException("Offer is not open");
        if (applications.existsByJobIdAndCandidateId(jobId, candidateId)) throw new IllegalStateException("Already applied");
        Application application = new Application();
        application.jobId = jobId;
        application.candidateId = candidateId;
        application.candidateCvId = cvId;
        application.status = Application.Status.PENDING;
        return applications.save(application);
    }

    public List<Application> candidateApplications(UUID candidateId) { return applications.findByCandidateId(candidateId); }
    public List<Application> recruiterApplications(UUID recruiterId) { return jobs.findByCreatedByUserId(recruiterId).stream().flatMap(j -> applications.findByJobId(j.id).stream()).toList(); }

    @Transactional
    public Application updateStatus(UUID id, Application.Status status, UUID recruiterId) {
        Application application = applications.findById(id).orElseThrow();
        ownedJob(application.jobId, recruiterId);
        application.status = status;
        return application;
    }

    private Job ownedJob(UUID id, UUID recruiterId) {
        Job job = jobs.findById(id).orElseThrow();
        if (!recruiterId.equals(job.createdByUserId)) throw new SecurityException("Job access denied");
        return job;
    }
}
