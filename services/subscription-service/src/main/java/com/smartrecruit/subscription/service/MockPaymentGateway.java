package com.smartrecruit.subscription.service;

import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class MockPaymentGateway implements PaymentGateway {
    @Override
    public PaymentResult charge(UUID ownerId, BigDecimal amount, String currency, String description) {
        return new PaymentResult(true, "MOCK-" + UUID.randomUUID());
    }
}
