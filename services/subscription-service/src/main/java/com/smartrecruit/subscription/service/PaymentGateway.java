package com.smartrecruit.subscription.service;

import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentGateway {
    PaymentResult charge(UUID ownerId, BigDecimal amount, String currency, String description);
    record PaymentResult(boolean approved, String reference) {}
}
