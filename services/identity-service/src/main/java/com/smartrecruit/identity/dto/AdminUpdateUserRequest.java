package com.smartrecruit.identity.dto;

import com.smartrecruit.identity.model.AccountStatus;

/** Partial update sent by the admin console: every field is optional. */
public record AdminUpdateUserRequest(String fullName, String phone, String address, String city, String state,
		String country, String postalCode, AccountStatus status) {
}
