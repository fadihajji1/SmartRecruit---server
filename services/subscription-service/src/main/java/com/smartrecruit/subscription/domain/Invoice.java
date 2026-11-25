package com.smartrecruit.subscription.domain;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "invoices")
public class Invoice {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name = "subscription_id", nullable = false) private UUID subscriptionId;
    @Column(name = "owner_id", nullable = false) private UUID ownerId;
    @Column(name = "plan_code", nullable = false, length = 40) private String planCode;
    @Column(nullable = false, precision = 10, scale = 2) private BigDecimal amount;
    @Column(nullable = false, length = 3) private String currency;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 16) private InvoiceStatus status;
    @Column(name = "payment_reference", nullable = false, length = 100) private String paymentReference;
    @Column(name = "issued_at", nullable = false) private Instant issuedAt;
    protected Invoice() {}
    public Invoice(UUID subscriptionId, UUID ownerId, String planCode, BigDecimal amount, String currency, String paymentReference) {
        this.subscriptionId = subscriptionId; this.ownerId = ownerId; this.planCode = planCode;
        this.amount = amount; this.currency = currency; this.paymentReference = paymentReference;
        this.status = InvoiceStatus.PAID; this.issuedAt = Instant.now();
    }
    public UUID getId() { return id; }
    public UUID getSubscriptionId() { return subscriptionId; }
    public UUID getOwnerId() { return ownerId; }
    public String getPlanCode() { return planCode; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public InvoiceStatus getStatus() { return status; }
    public String getPaymentReference() { return paymentReference; }
    public Instant getIssuedAt() { return issuedAt; }
}
