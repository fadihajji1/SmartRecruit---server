package com.smartrecruit.identity.dto;

import java.time.Instant;
import java.util.UUID;

import com.smartrecruit.identity.model.AccountStatus;
import com.smartrecruit.identity.model.User;
import com.smartrecruit.identity.model.UserRole;

public record UserResponse(
		UUID id,
		String email,
		String fullName,
		String phone,
		String address,
		String city,
		String state,
		String country,
		String postalCode,
		UserRole role,
		AccountStatus status,
		Instant createdAt) {

	public static UserResponse from(User user) {
		return new UserResponse(
			user.getId(), 
			user.getEmail(), 
			user.getFullName(),
			user.getPhone(), 
			user.getAddress(), 
			user.getCity(), 
			user.getState(), 
			user.getCountry(),
			user.getPostalCode(), 
			user.getRole(), 
			user.getStatus(), 
			user.getCreatedAt()
		);
	}
}
