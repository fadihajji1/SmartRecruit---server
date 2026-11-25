package com.smartrecruit.interview.room;

import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import com.smartrecruit.interview.model.Interview;
import com.smartrecruit.interview.model.InterviewStatus;
import com.smartrecruit.interview.repository.InterviewRepository;

/**
 * Lets someone into an interview room only if they are that interview's recruiter or candidate, the interview is
 * still scheduled, and the room is open (from {@code opens-minutes-before} the start until {@code closes-minutes-after}
 * the planned end). Browsers cannot set headers on a WebSocket, so the access token comes as a query parameter.
 */
@Component
public class InterviewRoomHandshakeInterceptor implements HandshakeInterceptor {
    public static final String INTERVIEW = "interviewId";
    public static final String ROLE = "role";
    public static final String NAME = "name";

    private final JwtDecoder jwtDecoder;
    private final InterviewRepository interviews;
    private final Duration opensBefore;
    private final Duration closesAfter;

    public InterviewRoomHandshakeInterceptor(JwtDecoder jwtDecoder, InterviewRepository interviews,
            @Value("${interview.rooms.opens-minutes-before:60}") long opensMinutesBefore,
            @Value("${interview.rooms.closes-minutes-after:120}") long closesMinutesAfter) {
        this.jwtDecoder = jwtDecoder;
        this.interviews = interviews;
        this.opensBefore = Duration.ofMinutes(opensMinutesBefore);
        this.closesAfter = Duration.ofMinutes(closesMinutesAfter);
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler handler,
            Map<String, Object> attributes) {
        URI uri = request.getURI();
        String token = UriComponentsBuilder.fromUri(uri).build().getQueryParams().getFirst("access_token");
        UUID interviewId = lastSegmentAsUuid(uri.getPath());
        if (token == null || token.isBlank() || interviewId == null) return refuse(response, HttpStatus.UNAUTHORIZED);

        Jwt jwt;
        UUID userId;
        try {
            jwt = jwtDecoder.decode(token);
            userId = UUID.fromString(jwt.getClaimAsString("user_id"));
        } catch (JwtException | IllegalArgumentException | NullPointerException invalid) {
            return refuse(response, HttpStatus.UNAUTHORIZED);
        }
        List<String> roles = jwt.getClaimAsStringList("roles");
        Interview interview = interviews.findById(interviewId).orElse(null);
        if (interview == null || roles == null) return refuse(response, HttpStatus.NOT_FOUND);

        String role;
        if (roles.contains("RECRUITER") && userId.equals(interview.getRecruiterId())) role = "RECRUITER";
        else if (roles.contains("CANDIDATE") && userId.equals(interview.getCandidateId())) role = "CANDIDATE";
        else return refuse(response, HttpStatus.FORBIDDEN);

        if (interview.getStatus() != InterviewStatus.SCHEDULED || !isOpen(interview, Instant.now())) {
            return refuse(response, HttpStatus.CONFLICT);
        }
        attributes.put(INTERVIEW, interviewId);
        attributes.put(ROLE, role);
        String name = jwt.getClaimAsString("name");
        attributes.put(NAME, name == null ? "" : name);
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler handler,
            Exception exception) {
        // nothing to clean up
    }

    boolean isOpen(Interview interview, Instant now) {
        Instant opens = interview.getScheduledAt().minus(opensBefore);
        Instant closes = interview.getScheduledAt().plus(Duration.ofMinutes(interview.getDurationMinutes())).plus(closesAfter);
        return !now.isBefore(opens) && !now.isAfter(closes);
    }

    private static boolean refuse(ServerHttpResponse response, HttpStatus status) {
        response.setStatusCode(status);
        return false;
    }

    private static UUID lastSegmentAsUuid(String path) {
        if (path == null) return null;
        String trimmed = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
        try {
            return UUID.fromString(trimmed.substring(trimmed.lastIndexOf('/') + 1));
        } catch (IllegalArgumentException invalid) {
            return null;
        }
    }
}
