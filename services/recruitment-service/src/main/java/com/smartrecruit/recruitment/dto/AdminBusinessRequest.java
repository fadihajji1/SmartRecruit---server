package com.smartrecruit.recruitment.dto;

import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Administrator create/update of a business; null fields are unchanged on update")
public record AdminBusinessRequest(
		@Schema(description = "Recruiter who owns the business (required on create)") UUID ownerUserId,
		@Size(max = 200) String name,
		@Size(max = 2000) String description,
		@Size(max = 500) String website,
		@Size(max = 120) String industry,
		@Size(max = 500) String address,
		@Size(max = 120) String city,
		@Size(max = 120) String country,
		@Schema(description = "ACTIVE or INACTIVE") String status) {
}
