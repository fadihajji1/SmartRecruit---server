package com.smartrecruit.interview;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

@SpringBootTest(properties = "identity.jwt.secret=local-test-secret-key-at-least-32-characters")
@AutoConfigureMockMvc
class InterviewWorkflowTests {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void recruiterCreatesCandidateRequestsAndRecruiterApprovesDelay() throws Exception {
        UUID recruiterId = UUID.randomUUID();
        UUID candidateId = UUID.randomUUID();
        UUID applicationId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        Instant scheduledAt = Instant.now().plusSeconds(3600).truncatedTo(ChronoUnit.SECONDS);
        Instant proposedAt = scheduledAt.plusSeconds(3600);

        String createResponse = mockMvc.perform(post("/api/v1/recruiter/interviews")
                .with(jwtFor(recruiterId, "RECRUITER"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                          "applicationId":"%s",
                          "candidateId":"%s",
                          "jobId":"%s",
                          "scheduledAt":"%s",
                          "externalMeetingUrl":"https://meet.google.com/abc-defg-hij"
                        }
                        """.formatted(applicationId, candidateId, jobId, scheduledAt)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.recruiterId").value(recruiterId.toString()))
                .andExpect(jsonPath("$.status").value("SCHEDULED"))
                .andReturn().getResponse().getContentAsString();

        String interviewId = com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
                .readTree(createResponse).get("id").asText();

        mockMvc.perform(post("/api/v1/candidate/interviews/{id}/delay-request", interviewId)
                .with(jwtFor(candidateId, "CANDIDATE"))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"proposedScheduledAt\":\"%s\"}".formatted(proposedAt)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"));

        mockMvc.perform(post("/api/v1/recruiter/interviews/{id}/delay-request/approve", interviewId)
                .with(jwtFor(recruiterId, "RECRUITER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));

        mockMvc.perform(get("/api/v1/candidate/interviews/{id}", interviewId)
                .with(jwtFor(candidateId, "CANDIDATE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scheduledAt").value(proposedAt.toString()));
    }

    @Test
    void candidateCannotUseRecruiterEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/recruiter/interviews")
                .with(jwtFor(UUID.randomUUID(), "CANDIDATE")))
                .andExpect(status().isForbidden());
    }

    private static RequestPostProcessor jwtFor(UUID userId, String role) {
        return jwt().jwt(token -> token.subject(userId + "@smartrecruit.test")
                .claim("user_id", userId.toString())
                .claim("roles", List.of(role)));
    }
}
