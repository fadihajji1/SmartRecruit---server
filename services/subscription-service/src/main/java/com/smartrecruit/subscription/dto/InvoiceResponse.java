package com.smartrecruit.subscription.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import com.smartrecruit.subscription.domain.Invoice;
import com.smartrecruit.subscription.domain.InvoiceStatus;

public record InvoiceResponse(UUID id, UUID subscriptionId, String planCode, BigDecimal amount,
        String currency, InvoiceStatus status, Instant issuedAt) {
    public static InvoiceResponse from(Invoice invoice) {
        return new InvoiceResponse(invoice.getId(), invoice.getSubscriptionId(), invoice.getPlanCode(),
            invoice.getAmount(), invoice.getCurrency(), invoice.getStatus(), invoice.getIssuedAt());
    }
}
