package com.smartrecruit.interview.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/** Settings the browser needs to open an interview room. */
@RestController
@RequestMapping("/api/v1/interviews")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Interview rooms", description = "In-platform video interviews")
public class RoomController {
    private final List<String> stunUrls;
    private final String turnUrl;
    private final String turnUsername;
    private final String turnCredential;
    private final long opensMinutesBefore;
    private final long closesMinutesAfter;

    public RoomController(@Value("${interview.rooms.stun-urls:stun:stun.l.google.com:19302,stun:stun1.l.google.com:19302}") List<String> stunUrls,
            @Value("${interview.rooms.turn-url:}") String turnUrl,
            @Value("${interview.rooms.turn-username:}") String turnUsername,
            @Value("${interview.rooms.turn-credential:}") String turnCredential,
            @Value("${interview.rooms.opens-minutes-before:60}") long opensMinutesBefore,
            @Value("${interview.rooms.closes-minutes-after:120}") long closesMinutesAfter) {
        this.stunUrls = stunUrls;
        this.turnUrl = turnUrl;
        this.turnUsername = turnUsername;
        this.turnCredential = turnCredential;
        this.opensMinutesBefore = opensMinutesBefore;
        this.closesMinutesAfter = closesMinutesAfter;
    }

    @GetMapping("/rtc-config")
    @Operation(summary = "ICE servers and opening hours for interview rooms")
    public Map<String, Object> rtcConfig() {
        List<Map<String, Object>> servers = new ArrayList<>();
        if (!stunUrls.isEmpty()) servers.add(Map.of("urls", stunUrls));
        if (!turnUrl.isBlank()) servers.add(Map.of("urls", List.of(turnUrl), "username", turnUsername, "credential", turnCredential));
        return Map.of("iceServers", servers, "opensMinutesBefore", opensMinutesBefore, "closesMinutesAfter", closesMinutesAfter);
    }
}
