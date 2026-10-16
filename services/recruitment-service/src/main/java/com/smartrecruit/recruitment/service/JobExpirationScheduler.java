package com.smartrecruit.recruitment.service;

import com.smartrecruit.recruitment.domain.Job;
import com.smartrecruit.recruitment.repo.JobRepository;
import java.time.Instant;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class JobExpirationScheduler {
    private final JobRepository jobs;
    public JobExpirationScheduler(JobRepository jobs) { this.jobs=jobs; }
    @Scheduled(fixedDelayString="${recruitment.jobs.expiration-check-ms:60000}")
    @Transactional
    public void expireOffers() {
        jobs.findByStatusAndExpiresAtBefore(Job.Status.PUBLISHED,Instant.now()).forEach(job->job.status=Job.Status.EXPIRED);
    }
}
