package com.smartrecruit.recruitment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Fields to update; omitted or null fields are unchanged")
public record UpdateBusinessRequest(
		@Schema(description = "Business name") @Size(max = 200) String name,
		@Schema(description = "Business description") @Size(max = 2000) String description,
		@Schema(description = "Business address") @Size(max = 500) String address,
		@Schema(description = "Website") @Size(max = 500) String website,
		@Schema(description = "Industry") @Size(max = 120) String industry,
		@Schema(description = "City") @Size(max = 120) String city,
		@Schema(description = "Country") @Size(max = 120) String country) {
}
