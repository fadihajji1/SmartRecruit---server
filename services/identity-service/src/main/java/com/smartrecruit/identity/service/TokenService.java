package com.smartrecruit.identity.service;

import java.time.Instant;
import java.util.List;

import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.stereotype.Service;

import com.smartrecruit.identity.dto.TokenResponse;
import com.smartrecruit.identity.config.IdentitySecurityProperties;
import com.smartrecruit.identity.model.User;

@Service
public class TokenService {
	private final JwtEncoder encoder;
	private final IdentitySecurityProperties properties;

	public TokenService(JwtEncoder encoder, IdentitySecurityProperties properties) {
		this.encoder = encoder;
		this.properties = properties;
	}

	public TokenResponse issue(User user) {
		Instant now = Instant.now();
		Instant expiresAt = now.plus(properties.expiration());
		JwtClaimsSet claims = JwtClaimsSet.builder()
				.issuer(properties.issuer())
				.issuedAt(now)
				.expiresAt(expiresAt)
				.subject(user.getEmail())
				.claim("user_id", user.getId().toString())
				.claim("roles", List.of(user.getRole().name()))
				.build();
		JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
		String token = encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
		return new TokenResponse(token, "Bearer", expiresAt);
	}
}
