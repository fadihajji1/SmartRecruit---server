package com.smartrecruit.recruitment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Company profile created by a recruiter, who becomes its owner")
public record CreateBusinessRequest(
		@Schema(example = "Carthage FinTech", requiredMode = Schema.RequiredMode.REQUIRED) @NotBlank @Size(max = 200) String name,
		@Size(max = 2000) String description,
		@Schema(example = "https://carthage-fintech.example") @Size(max = 500) String website,
		@Schema(example = "Financial technology") @Size(max = 120) String industry,
		@Size(max = 500) String address,
		@Schema(example = "Tunis") @Size(max = 120) String city,
		@Schema(example = "Tunisia") @Size(max = 120) String country) {
}
