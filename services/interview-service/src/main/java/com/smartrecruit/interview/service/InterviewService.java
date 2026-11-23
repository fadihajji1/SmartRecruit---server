package com.smartrecruit.interview.service;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartrecruit.interview.client.RecruitmentClient;
import com.smartrecruit.interview.events.InterviewEvents;
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
    private final RecruitmentClient recruitment;
    private final InterviewEvents events;

    public InterviewService(InterviewRepository interviews, DelayRequestRepository delayRequests,
            RecruitmentClient recruitment, InterviewEvents events) {
        this.interviews = interviews;
        this.delayRequests = delayRequests;
        this.recruitment = recruitment;
        this.events = events;
    }

    private InterviewResponse view(Interview interview) {
        return InterviewResponse.from(interview,
                delayRequests.findFirstByInterviewIdAndStatusOrderByCreatedAtDesc(interview.getId(),
                        DelayRequestStatus.PENDING).orElse(null));
    }

    @Transactional(readOnly = true)
    public List<InterviewResponse> getRecruiterInterviews(UUID recruiterId) {
        return interviews.findByRecruiterIdOrderByScheduledAtAsc(recruiterId).stream()
                .map(this::view).toList();
    }

    @Transactional(readOnly = true)
    public List<InterviewResponse> getCandidateInterviews(UUID candidateId) {
        return interviews.findByCandidateIdOrderByScheduledAtAsc(candidateId).stream()
                .map(this::view).toList();
    }

    @Transactional
    public InterviewResponse create(UUID recruiterId, String bearerToken, CreateInterviewRequest request) {
        validateMeetingUrl(request.externalMeetingUrl());
        RecruitmentClient.ApplicationSummary application =
                recruitment.recruiterApplication(request.applicationId(), bearerToken);
        if ("UNAVAILABLE".equals(application.status()) || "REJECTED".equals(application.status())) {
            throw new InvalidInterviewStateException("Interviews cannot be scheduled for a " 
                    + application.status().toLowerCase() + " application");
        }
        Interview interview = new Interview(request.applicationId(), application.candidateId(), recruiterId,
                application.jobId(), request.scheduledAt(), request.externalMeetingUrl().trim());
        interview.describe(application.jobTitle(), application.candidateName(), request.durationMinutes(),
                request.notes());
        interview = interviews.save(interview);
        events.publish("INTERVIEW_SCHEDULED", interview, interview.getCandidateId(), null);
        return view(interview);
    }

    @Transactional(readOnly = true)
    public InterviewResponse getForRecruiter(UUID recruiterId, UUID interviewId) {
        return view(interviews.findByIdAndRecruiterId(interviewId, recruiterId)
                .orElseThrow(InterviewNotFoundException::new));
    }

    @Transactional(readOnly = true)
    public InterviewResponse getForCandidate(UUID candidateId, UUID interviewId) {
        return view(interviews.findByIdAndCandidateId(interviewId, candidateId)
                .orElseThrow(InterviewNotFoundException::new));
    }

    @Transactional
    public InterviewResponse update(UUID recruiterId, UUID interviewId, UpdateInterviewRequest request) {
        validateMeetingUrl(request.externalMeetingUrl());
        Interview interview = recruiterInterview(recruiterId, interviewId);
        ensureScheduled(interview);
        boolean rescheduled = !request.scheduledAt().equals(interview.getScheduledAt());
        interview.update(request.scheduledAt(), request.externalMeetingUrl().trim());
        interview.describe(null, null, request.durationMinutes(), request.notes());
        interview = interviews.save(interview);
        events.publish(rescheduled ? "INTERVIEW_RESCHEDULED" : "INTERVIEW_UPDATED", interview,
                interview.getCandidateId(), null);
        return view(interview);
    }

    @Transactional
    public InterviewResponse cancelAsRecruiter(UUID recruiterId, UUID interviewId) {
        Interview interview = recruiterInterview(recruiterId, interviewId);
        ensureScheduled(interview);
        interview.cancel();
        interview = interviews.save(interview);
        events.publish("INTERVIEW_CANCELLED", interview, interview.getCandidateId(), null);
        return view(interview);
    }

    @Transactional
    public InterviewResponse cancelAsCandidate(UUID candidateId, UUID interviewId) {
        Interview interview = interviews.findByIdAndCandidateId(interviewId, candidateId)
                .orElseThrow(InterviewNotFoundException::new);
        ensureScheduled(interview);
        interview.cancel();
        interview = interviews.save(interview);
        events.publish("INTERVIEW_CANCELLED", interview, interview.getRecruiterId(), null);
        return view(interview);
    }

    @Transactional
    public InterviewResponse markDone(UUID recruiterId, UUID interviewId) {
        Interview interview = recruiterInterview(recruiterId, interviewId);
        ensureScheduled(interview);
        interview.markDone();
        interview = interviews.save(interview);
        events.publish("INTERVIEW_COMPLETED", interview, interview.getCandidateId(), null);
        return view(interview);
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
        events.publish("INTERVIEW_DELAY_REQUESTED", interview, interview.getRecruiterId(), request.proposedScheduledAt());
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
        DelayRequestResponse response = DelayRequestResponse.from(delayRequests.save(request), interviewId);
        events.publish(approve ? "INTERVIEW_DELAY_APPROVED" : "INTERVIEW_DELAY_REJECTED", interview,
                interview.getCandidateId(), request.getProposedScheduledAt());
        return response;
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
                    || host.equals("meet.google.com")
                    || host.equals("teams.microsoft.com") || host.equals("teams.live.com")
                    || host.endsWith(".webex.com");
            if (!"https".equalsIgnoreCase(uri.getScheme()) || !supportedHost) {
                throw new IllegalArgumentException("externalMeetingUrl must be an HTTPS Zoom, Google Meet, Microsoft Teams or Webex link");
            }
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("externalMeetingUrl must be an HTTPS Zoom, Google Meet, Microsoft Teams or Webex link");
        }
    }
}
