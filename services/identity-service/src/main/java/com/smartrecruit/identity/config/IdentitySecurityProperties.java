package com.smartrecruit.identity.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("identity.jwt")
public record IdentitySecurityProperties(String secret, String issuer, Duration expiration) {
	public IdentitySecurityProperties {
		if (secret == null || secret.length() < 32) {
			throw new IllegalArgumentException("identity.jwt.secret must contain at least 32 characters");
		}
	}
}
