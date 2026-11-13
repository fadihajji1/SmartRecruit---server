package com.smartrecruit.interview.service;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartrecruit.interview.dto.CreateInterviewRequest;
import com.smartrecruit.interview.dto.DelayRequestDto;
import com.smartrecruit.interview.dto.DelayRequestResponse;
import com.smartrecruit.interview.dto.InterviewResponse;
import com.smartrecruit.interview.dto.UpdateInterviewRequest;
import com.smartrecruit.interview.exception.DelayRequestNotFoundException;
import com.smartrecruit.interview.exception.InterviewNotFoundException;
import com.smartrecruit.interview.exception.InvalidInterviewStateException;
import com.smartrecruit.interview.model.DelayRequest;
import com.smartrecruit.interview.model.DelayRequestStatus;
import com.smartrecruit.interview.model.Interview;
import com.smartrecruit.interview.model.InterviewStatus;
import com.smartrecruit.interview.repository.DelayRequestRepository;
import com.smartrecruit.interview.repository.InterviewRepository;

@Service
public class InterviewService {
    private final InterviewRepository interviews;
    private final DelayRequestRepository delayRequests;

    public InterviewService(InterviewRepository interviews, DelayRequestRepository delayRequests) {
        this.interviews = interviews;
        this.delayRequests = delayRequests;
    }

    @Transactional(readOnly = true)
    public List<InterviewResponse> getRecruiterInterviews(UUID recruiterId) {
        return interviews.findByRecruiterIdOrderByScheduledAtAsc(recruiterId).stream()
                .map(InterviewResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<InterviewResponse> getCandidateInterviews(UUID candidateId) {
        return interviews.findByCandidateIdOrderByScheduledAtAsc(candidateId).stream()
                .map(InterviewResponse::from).toList();
    }

    @Transactional
    public InterviewResponse create(UUID recruiterId, CreateInterviewRequest request) {
        validateMeetingUrl(request.externalMeetingUrl());
        return InterviewResponse.from(interviews.save(new Interview(request.applicationId(), request.candidateId(),
                recruiterId, request.jobId(), request.scheduledAt(), request.externalMeetingUrl().trim())));
    }

    @Transactional(readOnly = true)
    public InterviewResponse getForRecruiter(UUID recruiterId, UUID interviewId) {
        return InterviewResponse.from(interviews.findByIdAndRecruiterId(interviewId, recruiterId)
                .orElseThrow(InterviewNotFoundException::new));
    }

    @Transactional(readOnly = true)
    public InterviewResponse getForCandidate(UUID candidateId, UUID interviewId) {
        return InterviewResponse.from(interviews.findByIdAndCandidateId(interviewId, candidateId)
                .orElseThrow(InterviewNotFoundException::new));
    }

    @Transactional
    public InterviewResponse update(UUID recruiterId, UUID interviewId, UpdateInterviewRequest request) {
        validateMeetingUrl(request.externalMeetingUrl());
        Interview interview = recruiterInterview(recruiterId, interviewId);
        ensureScheduled(interview);
        interview.update(request.scheduledAt(), request.externalMeetingUrl().trim());
        return InterviewResponse.from(interviews.save(interview));
    }

    @Transactional
    public InterviewResponse cancelAsRecruiter(UUID recruiterId, UUID interviewId) {
        Interview interview = recruiterInterview(recruiterId, interviewId);
        ensureScheduled(interview);
        interview.cancel();
        return InterviewResponse.from(interviews.save(interview));
    }

    @Transactional
    public InterviewResponse cancelAsCandidate(UUID candidateId, UUID interviewId) {
        Interview interview = interviews.findByIdAndCandidateId(interviewId, candidateId)
                .orElseThrow(InterviewNotFoundException::new);
        ensureScheduled(interview);
        interview.cancel();
        return InterviewResponse.from(interviews.save(interview));
    }

    @Transactional
    public InterviewResponse markDone(UUID recruiterId, UUID interviewId) {
        Interview interview = recruiterInterview(recruiterId, interviewId);
        ensureScheduled(interview);
        interview.markDone();
        return InterviewResponse.from(interviews.save(interview));
    }

    @Transactional
    public DelayRequestResponse requestDelay(UUID candidateId, UUID interviewId, DelayRequestDto request) {
        Interview interview = interviews.findByIdAndCandidateId(interviewId, candidateId)
                .orElseThrow(InterviewNotFoundException::new);
        ensureScheduled(interview);
        if (delayRequests.existsByInterviewIdAndRequestedByUserIdAndStatus(interviewId, candidateId,
                DelayRequestStatus.PENDING)) {
            throw new InvalidInterviewStateException("A delay request is already pending");
        }
        DelayRequest saved = delayRequests.save(new DelayRequest(interview, candidateId, request.proposedScheduledAt()));
        return DelayRequestResponse.from(saved, interviewId);
    }

    @Transactional
    public DelayRequestResponse resolveDelay(UUID recruiterId, UUID interviewId, boolean approve) {
        Interview interview = recruiterInterview(recruiterId, interviewId);
        ensureScheduled(interview);
        DelayRequest request = delayRequests
                .findFirstByInterviewIdAndInterviewRecruiterIdAndStatusOrderByCreatedAtDesc(interviewId, recruiterId,
                        DelayRequestStatus.PENDING)
                .orElseThrow(DelayRequestNotFoundException::new);
        if (approve) {
            interview.update(request.getProposedScheduledAt(), interview.getExternalMeetingUrl());
            request.approve();
            interviews.save(interview);
        } else {
            request.reject();
        }
        return DelayRequestResponse.from(delayRequests.save(request), interviewId);
    }

    private Interview recruiterInterview(UUID recruiterId, UUID interviewId) {
        return interviews.findByIdAndRecruiterId(interviewId, recruiterId)
                .orElseThrow(InterviewNotFoundException::new);
    }

    private static void ensureScheduled(Interview interview) {
        if (interview.getStatus() != InterviewStatus.SCHEDULED) {
            throw new InvalidInterviewStateException("Only scheduled interviews can be changed");
        }
    }

    private static void validateMeetingUrl(String value) {
        try {
            URI uri = URI.create(value.trim());
            String host = uri.getHost() == null ? "" : uri.getHost().toLowerCase();
            boolean supportedHost = host.equals("zoom.us") || host.endsWith(".zoom.us")
                    || host.equals("meet.google.com") || host.endsWith(".google.com");
            if (!"https".equalsIgnoreCase(uri.getScheme()) || !supportedHost) {
                throw new IllegalArgumentException("externalMeetingUrl must be a supported HTTPS Zoom or Google Meet URL");
            }
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("externalMeetingUrl must be a supported HTTPS Zoom or Google Meet URL");
        }
    }
}
