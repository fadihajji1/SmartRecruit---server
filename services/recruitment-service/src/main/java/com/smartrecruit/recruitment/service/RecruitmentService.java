package com.smartrecruit.recruitment.service;

import com.smartrecruit.recruitment.domain.*;
import com.smartrecruit.recruitment.repo.*;
import com.smartrecruit.recruitment.events.OutboxPublisher;
import java.util.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RecruitmentService {
    private final JobRepository jobs;
    private final ApplicationRepository applications;
    private final BusinessRepository businesses;
    private final OutboxPublisher outbox;
    public RecruitmentService(JobRepository jobs, ApplicationRepository applications, BusinessRepository businesses,OutboxPublisher outbox) {
        this.jobs=jobs;this.applications=applications;this.businesses=businesses;this.outbox=outbox;
    }

    public Page<Job> publicJobs(String query,String location,String contractType,int page,int size) {
        int safePage=Math.max(page,0), safeSize=Math.min(Math.max(size,1),100);
        Pageable pageable=PageRequest.of(safePage,safeSize,Sort.by("createdAt").descending());
        return jobs.searchPublished(Job.Status.PUBLISHED,normalize(query),normalize(location),normalize(contractType),pageable);
    }
    private String normalize(String value) { return value==null||value.isBlank()?null:value.trim(); }
    public Job publicJob(UUID id) { Job job=jobs.findById(id).orElseThrow();if(job.status!=Job.Status.PUBLISHED)throw new NoSuchElementException();return job; }

    @Transactional public Business createBusiness(Business b,UUID owner) { b.id=null;b.ownerUserId=owner;b.status=Business.Status.ACTIVE;return businesses.save(b); }
    public List<Business> recruiterBusinesses(UUID owner) { return businesses.findByOwnerUserId(owner); }
    public List<Business> allBusinesses() { return businesses.findAll(); }
    public Business adminBusiness(UUID id) { return businesses.findById(id).orElseThrow(); }
    @Transactional public Business adminCreateBusiness(Business b) { b.id=null;if(b.ownerUserId==null)throw new IllegalArgumentException("ownerUserId is required");if(b.status==null)b.status=Business.Status.ACTIVE;return businesses.save(b); }
    @Transactional public void deactivateBusiness(UUID id) { Business b=businesses.findById(id).orElseThrow();b.status=Business.Status.INACTIVE; }
    @Transactional public void deactivateBusiness(UUID id,UUID owner) { Business b=business(id,owner);b.status=Business.Status.INACTIVE; }
    public Business business(UUID id,UUID owner) { Business b=businesses.findById(id).orElseThrow();if(!b.ownerUserId.equals(owner))throw new SecurityException("Business access denied");return b; }
    @Transactional public Business updateBusiness(UUID id,Business input,UUID owner) {
        Business b=business(id,owner);b.name=input.name;b.description=input.description;b.website=input.website;b.industry=input.industry;
        b.address=input.address;b.city=input.city;b.country=input.country;return b;
    }
    @Transactional public Business adminUpdateBusiness(UUID id,Business input) {
        Business b=businesses.findById(id).orElseThrow();if(input.name!=null)b.name=input.name;if(input.description!=null)b.description=input.description;
        if(input.website!=null)b.website=input.website;if(input.industry!=null)b.industry=input.industry;if(input.address!=null)b.address=input.address;
        if(input.city!=null)b.city=input.city;if(input.country!=null)b.country=input.country;if(input.status!=null)b.status=input.status;return b;
    }

    @Transactional public Job createJob(Job job,UUID recruiterId) {
        job.id=null;job.createdByUserId=recruiterId;
        if(job.businessId==null)throw new IllegalArgumentException("Business is required");
        if(business(job.businessId,recruiterId).status!=Business.Status.ACTIVE)throw new IllegalStateException("Business is inactive");
        if(job.expiresAt!=null && !job.expiresAt.isAfter(java.time.Instant.now()))throw new IllegalArgumentException("Offer expiration must be in the future");
        job.status=Job.Status.DRAFT;
        return jobs.save(job);
    }
    public List<Job> recruiterJobs(UUID recruiter) { return jobs.findByCreatedByUserId(recruiter); }
    public Job recruiterJob(UUID id,UUID recruiter) { return ownedJob(id,recruiter); }
    @Transactional public Job updateJob(UUID id,Job input,UUID recruiterId) {
        Job job=ownedJob(id,recruiterId);if(job.status==Job.Status.DELETED)throw new IllegalStateException("Deleted jobs cannot be changed");
        if(input.businessId==null)throw new IllegalArgumentException("Business is required");
        if(business(input.businessId,recruiterId).status!=Business.Status.ACTIVE)throw new IllegalStateException("Business is inactive");
        if(input.expiresAt!=null && !input.expiresAt.isAfter(java.time.Instant.now()))throw new IllegalArgumentException("Offer expiration must be in the future");
        job.businessId=input.businessId;job.title=input.title;job.description=input.description;job.location=input.location;job.contractType=input.contractType;job.expiresAt=input.expiresAt;return job;
    }
    @Transactional public Job transition(UUID id,UUID recruiterId,Job.Status status) {
        Job job=ownedJob(id,recruiterId);
        boolean allowed=switch(status) {
            case PUBLISHED -> job.status==Job.Status.DRAFT || job.status==Job.Status.CLOSED;
            case CLOSED -> job.status==Job.Status.PUBLISHED;
            case DELETED -> job.status!=Job.Status.DELETED;
            default -> false;
        };
        if(!allowed)throw new IllegalStateException("Invalid job status transition");
        if(status==Job.Status.PUBLISHED && job.expiresAt!=null && !job.expiresAt.isAfter(java.time.Instant.now()))throw new IllegalStateException("Expired offers cannot be published");
        job.status=status;
        if(status==Job.Status.PUBLISHED)job.publishedAt=java.time.Instant.now();
        if(status==Job.Status.DELETED)applications.findByJobId(id).forEach(a->a.status=Application.Status.UNAVAILABLE);
        return job;
    }
    @Transactional public Application apply(UUID jobId,UUID cvId,String message,UUID candidateId) {
        Job job=jobs.findById(jobId).orElseThrow();if(job.status!=Job.Status.PUBLISHED)throw new IllegalStateException("Offer is not open");
        if(cvId==null)throw new IllegalArgumentException("Candidate CV is required");
        if(applications.existsByJobIdAndCandidateId(jobId,candidateId))throw new IllegalStateException("Already applied");
        Application a=new Application();a.jobId=jobId;a.candidateId=candidateId;a.candidateCvId=cvId;a.status=Application.Status.PENDING;
        a.message=message;a=applications.save(a);
        outbox.enqueue("APPLICATION_SUBMITTED",List.of(candidateId,job.createdByUserId),"New job application",
            "A candidate submitted an application for " + job.title + ".");
        return a;
    }
    public List<Application> candidateApplications(UUID id) { return applications.findByCandidateId(id); }
    public Application candidateApplication(UUID id,UUID candidate) { return applications.findByIdAndCandidateId(id,candidate).orElseThrow(); }
    public List<Application> recruiterApplications(UUID recruiter) {
        return jobs.findByCreatedByUserId(recruiter).stream().flatMap(j->applications.findByJobId(j.id).stream()).toList();
    }
    public Application recruiterApplication(UUID id,UUID recruiter) { Application a=applications.findById(id).orElseThrow();ownedJob(a.jobId,recruiter);return a; }
    @Transactional public Application updateStatus(UUID id,Application.Status status,UUID recruiter) {
        Application a=recruiterApplication(id,recruiter);if(status==Application.Status.UNAVAILABLE)throw new IllegalArgumentException("Unavailable is a system-managed status");
        Application.Status previous=a.status;a.status=status;
        if(previous!=status)outbox.enqueue("APPLICATION_STATUS_CHANGED",List.of(a.candidateId),"Application status updated",
            "Your application status changed from " + previous + " to " + status + ".");
        return a;
    }
    private Job ownedJob(UUID id,UUID recruiter) { Job j=jobs.findById(id).orElseThrow();if(!recruiter.equals(j.createdByUserId))throw new SecurityException("Job access denied");return j; }
}
