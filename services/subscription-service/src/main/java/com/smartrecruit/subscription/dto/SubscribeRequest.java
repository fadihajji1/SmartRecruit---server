package com.smartrecruit.subscription.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SubscribeRequest(@NotBlank @Size(max = 40) String planCode, boolean autoRenew) {}
