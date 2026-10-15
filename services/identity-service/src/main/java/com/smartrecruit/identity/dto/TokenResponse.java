package com.smartrecruit.identity.dto;

import java.time.Instant;

public record TokenResponse(String accessToken, String tokenType, Instant expiresAt) {
}
