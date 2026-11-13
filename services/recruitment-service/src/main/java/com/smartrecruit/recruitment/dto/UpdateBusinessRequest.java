package com.smartrecruit.recruitment.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Fields to update; omitted or null fields are unchanged")
public record UpdateBusinessRequest(
		@Schema(description = "Business name") String name,
		@Schema(description = "Business description") String description,
		@Schema(description = "Business address") String address) {
}
