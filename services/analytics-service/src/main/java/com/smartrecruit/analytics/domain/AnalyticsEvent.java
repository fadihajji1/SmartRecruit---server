package com.smartrecruit.analytics.domain;

import java.time.Instant;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name="analytics_events")
public class AnalyticsEvent {
    @Id @GeneratedValue(strategy=GenerationType.UUID) private UUID id;
    @Column(name="event_id",nullable=false,unique=true) private UUID eventId;
    @Column(name="event_type",nullable=false,length=80) private String eventType;
    @Column(name="aggregate_id") private UUID aggregateId;
    @Column(name="business_id") private UUID businessId;
    @Column(name="recruiter_id") private UUID recruiterId;
    @Column(name="candidate_id") private UUID candidateId;
    @Column(name="job_id") private UUID jobId;
    @Column(name="application_id") private UUID applicationId;
    @Column(name="interview_id") private UUID interviewId;
    @Column(length=24) private String status;
    @Column(length=160) private String title;
    @Column(name="occurred_at",nullable=false) private Instant occurredAt;
    protected AnalyticsEvent() {}
    public AnalyticsEvent(UUID eventId,String eventType,UUID aggregateId,UUID businessId,UUID recruiterId,UUID candidateId,
            UUID jobId,UUID applicationId,UUID interviewId,String status,String title,Instant occurredAt) {
        this.eventId=eventId;this.eventType=eventType;this.aggregateId=aggregateId;this.businessId=businessId;
        this.recruiterId=recruiterId;this.candidateId=candidateId;this.jobId=jobId;this.applicationId=applicationId;
        this.interviewId=interviewId;this.status=status;this.title=title;this.occurredAt=occurredAt;
    }
    public UUID getId(){return id;} public UUID getEventId(){return eventId;} public String getEventType(){return eventType;}
    public UUID getAggregateId(){return aggregateId;} public UUID getBusinessId(){return businessId;} public UUID getRecruiterId(){return recruiterId;}
    public UUID getCandidateId(){return candidateId;} public UUID getJobId(){return jobId;} public UUID getApplicationId(){return applicationId;}
    public UUID getInterviewId(){return interviewId;} public String getStatus(){return status;} public String getTitle(){return title;} public Instant getOccurredAt(){return occurredAt;}
}
