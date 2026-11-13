package com.smartrecruit.identity.service;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartrecruit.identity.dto.UpdateProfileRequest;
import com.smartrecruit.identity.dto.UserResponse;
import com.smartrecruit.identity.repository.UserRepository;

@Service
public class ProfileService {
	private final UserRepository users;

	public ProfileService(UserRepository users) {
		this.users = users;
	}

	@Transactional(readOnly = true)
	public UserResponse getProfile(Jwt jwt) {
		return users.findByEmailIgnoreCase(jwt.getSubject())
				.map(UserResponse::from)
				.orElseThrow(UserNotFoundException::new);
	}

	@Transactional
	public UserResponse updateProfile(Jwt jwt, UpdateProfileRequest request) {
		var user = users.findByEmailIgnoreCase(jwt.getSubject()).orElseThrow(UserNotFoundException::new);
		user.setProfile(request.fullName().trim(), request.phone(), request.address(),
				request.city(), request.state(), request.country(), request.postalCode());
		return UserResponse.from(users.save(user));
	}
}