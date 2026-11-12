package com.smartrecruit.subscription.service;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.smartrecruit.subscription.domain.Invoice;
import com.smartrecruit.subscription.domain.Subscription;
import com.smartrecruit.subscription.domain.SubscriptionAudience;
import com.smartrecruit.subscription.domain.SubscriptionHistory;
import com.smartrecruit.subscription.domain.SubscriptionPlan;
import com.smartrecruit.subscription.domain.SubscriptionStatus;
import com.smartrecruit.subscription.dto.AdminSubscriptionUpdateRequest;
import com.smartrecruit.subscription.dto.InvoiceResponse;
import com.smartrecruit.subscription.dto.PlanRequest;
import com.smartrecruit.subscription.dto.PlanResponse;
import com.smartrecruit.subscription.dto.SubscribeRequest;
import com.smartrecruit.subscription.dto.SubscriptionHistoryResponse;
import com.smartrecruit.subscription.dto.SubscriptionResponse;
import com.smartrecruit.subscription.repository.InvoiceRepository;
import com.smartrecruit.subscription.repository.SubscriptionHistoryRepository;
import com.smartrecruit.subscription.repository.SubscriptionPlanRepository;
import com.smartrecruit.subscription.repository.SubscriptionRepository;

@Service
public class SubscriptionService {
    private static final List<SubscriptionStatus> CURRENT_STATUSES = List.of(
        SubscriptionStatus.TRIALING, SubscriptionStatus.ACTIVE, SubscriptionStatus.PAST_DUE, SubscriptionStatus.SUSPENDED);

    private final SubscriptionPlanRepository plans;
    private final SubscriptionRepository subscriptions;
    private final SubscriptionHistoryRepository history;
    private final InvoiceRepository invoices;
    private final PaymentGateway payments;

    public SubscriptionService(SubscriptionPlanRepository plans, SubscriptionRepository subscriptions,
            SubscriptionHistoryRepository history, InvoiceRepository invoices, PaymentGateway payments) {
        this.plans = plans; this.subscriptions = subscriptions; this.history = history;
        this.invoices = invoices; this.payments = payments;
    }

    @Transactional(readOnly = true)
    public List<PlanResponse> listPlans(SubscriptionAudience audience) {
        return (audience == null ? plans.findAllByActiveTrueOrderByPriceAsc()
                : plans.findAllByAudienceAndActiveTrueOrderByPriceAsc(audience))
            .stream().map(PlanResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<PlanResponse> listAllPlans() {
        return plans.findAllByOrderByAudienceAscPriceAsc().stream().map(PlanResponse::from).toList();
    }

    @Transactional
    public PlanResponse createPlan(PlanRequest request) {
        String code = request.planCode().trim().toUpperCase(java.util.Locale.ROOT);
        if (plans.findByPlanCodeIgnoreCase(code).isPresent()) throw new ResponseStatusException(HttpStatus.CONFLICT, "Plan code already exists");
        SubscriptionPlan plan = new SubscriptionPlan(code, request.displayName(), request.description(), request.audience(),
            request.price(), request.currency(), request.billingPeriodDays(), request.trialDays(), request.features());
        return PlanResponse.from(plans.save(plan));
    }

    @Transactional
    public PlanResponse updatePlan(UUID planId, PlanRequest request) {
        SubscriptionPlan plan = requirePlan(planId);
        if (plan.getAudience() != request.audience()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A plan audience cannot be changed");
        plan.update(request.displayName(), request.description(), request.price(), request.currency(),
            request.billingPeriodDays(), request.trialDays(), request.features(), request.active() == null || request.active());
        return PlanResponse.from(plans.save(plan));
    }

    @Transactional
    public SubscriptionResponse current(UUID ownerId, SubscriptionAudience audience) {
        Subscription subscription = findCurrent(ownerId, audience).orElse(null);
        if (subscription == null) {
            if (audience != SubscriptionAudience.CANDIDATE) throw notFound("No current subscription was found");
            SubscriptionPlan freePlan = requireActivePlan("CANDIDATE_FREE", SubscriptionAudience.CANDIDATE);
            Instant now = Instant.now();
            subscription = subscriptions.save(new Subscription(ownerId, audience, freePlan,
                SubscriptionStatus.ACTIVE, now, now.plusSeconds((long) freePlan.getBillingPeriodDays() * 86400L), false));
            record(subscription, ownerId, "DEFAULT_ASSIGNED", null, SubscriptionStatus.ACTIVE.name(),
                null, freePlan.getPlanCode(), "Default candidate plan assigned");
        }
        return SubscriptionResponse.from(subscriptions.save(subscription));
    }

    @Transactional
    public SubscriptionResponse subscribe(UUID ownerId, SubscriptionAudience audience, SubscribeRequest request) {
        if (findCurrent(ownerId, audience).isPresent()) throw new ResponseStatusException(HttpStatus.CONFLICT, "An active subscription already exists");
        SubscriptionPlan plan = requireActivePlan(request.planCode(), audience);
        Instant now = Instant.now();
        boolean trial = plan.getTrialDays() > 0;
        Instant endsAt = now.plusSeconds((long) (trial ? plan.getTrialDays() : plan.getBillingPeriodDays()) * 86400L);
        Subscription subscription = subscriptions.save(new Subscription(ownerId, audience, plan,
            trial ? SubscriptionStatus.TRIALING : SubscriptionStatus.ACTIVE, now, endsAt, request.autoRenew()));
        if (!trial) charge(subscription, plan, "subscription start");
        record(subscription, ownerId, "CREATED", null, null, null, plan.getPlanCode(), null);
        return SubscriptionResponse.from(subscription);
    }

    @Transactional
    public SubscriptionResponse upgrade(UUID ownerId, SubscriptionAudience audience, SubscribeRequest request) {
        Subscription subscription = requireCurrent(ownerId, audience);
        SubscriptionPlan next = requireActivePlan(request.planCode(), audience);
        if (next.getPrice().compareTo(subscription.getPlan().getPrice()) <= 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose a higher-priced plan to upgrade");
        String oldCode = subscription.getPlan().getPlanCode();
        SubscriptionStatus oldStatus = subscription.getStatus();
        charge(subscription, next, "subscription upgrade");
        Instant now = Instant.now();
        subscription.upgrade(next, now, now.plusSeconds((long) next.getBillingPeriodDays() * 86400L), request.autoRenew());
        record(subscription, ownerId, "UPGRADED", oldStatus.name(), subscription.getStatus().name(), oldCode, next.getPlanCode(), "Plan changed immediately");
        return SubscriptionResponse.from(subscriptions.save(subscription));
    }

    @Transactional
    public SubscriptionResponse downgrade(UUID ownerId, SubscriptionAudience audience, SubscribeRequest request) {
        Subscription subscription = requireCurrent(ownerId, audience);
        SubscriptionPlan next = requireActivePlan(request.planCode(), audience);
        if (next.getPrice().compareTo(subscription.getPlan().getPrice()) >= 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose a lower-priced plan to downgrade");
        if (subscription.getPendingPlan() != null) throw new ResponseStatusException(HttpStatus.CONFLICT, "A plan change is already scheduled");
        subscription.scheduleDowngrade(next);
        record(subscription, ownerId, "DOWNGRADE_SCHEDULED", subscription.getStatus().name(), subscription.getStatus().name(),
            subscription.getPlan().getPlanCode(), next.getPlanCode(), "Plan changes at the end of the current period");
        return SubscriptionResponse.from(subscriptions.save(subscription));
    }

    @Transactional
    public SubscriptionResponse renew(UUID ownerId, SubscriptionAudience audience) {
        Subscription subscription = subscriptions.findFirstByOwnerIdAndAudienceAndStatusInOrderByCreatedAtDesc(ownerId, audience,
            List.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.TRIALING, SubscriptionStatus.CANCELLED, SubscriptionStatus.EXPIRED, SubscriptionStatus.PAST_DUE))
            .orElseThrow(() -> notFound("No subscription history was found"));
        SubscriptionStatus oldStatus = subscription.getStatus();
        String planCode = subscription.getPlan().getPlanCode();
        charge(subscription, subscription.getPlan(), "subscription renewal");
        subscription.renew(Instant.now());
        record(subscription, ownerId, "RENEWED", oldStatus.name(), subscription.getStatus().name(), planCode, planCode, null);
        return SubscriptionResponse.from(subscriptions.save(subscription));
    }

    @Transactional
    public SubscriptionResponse cancel(UUID ownerId, SubscriptionAudience audience) {
        Subscription subscription = requireCurrent(ownerId, audience);
        SubscriptionStatus oldStatus = subscription.getStatus();
        String planCode = subscription.getPlan().getPlanCode();
        subscription.cancel(Instant.now());
        record(subscription, ownerId, "CANCELLED", oldStatus.name(), subscription.getStatus().name(), planCode, planCode, null);
        return SubscriptionResponse.from(subscriptions.save(subscription));
    }

    @Transactional(readOnly = true)
    public Page<SubscriptionHistoryResponse> history(UUID ownerId, SubscriptionAudience audience, Pageable pageable) {
        return history.findAllByOwnerAndAudience(ownerId, audience, pageable).map(SubscriptionHistoryResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<InvoiceResponse> invoices(UUID ownerId, Pageable pageable) {
        return invoices.findAllByOwnerIdOrderByIssuedAtDesc(ownerId, pageable).map(InvoiceResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<SubscriptionResponse> companySubscriptions(Pageable pageable) {
        return subscriptions.findAllByAudienceOrderByCreatedAtDesc(SubscriptionAudience.RECRUITER, pageable).map(SubscriptionResponse::from);
    }

    @Transactional
    public SubscriptionResponse adminUpdate(UUID subscriptionId, UUID actorId, AdminSubscriptionUpdateRequest request) {
        Subscription subscription = subscriptions.findById(subscriptionId).orElseThrow(() -> notFound("Subscription was not found"));
        SubscriptionStatus oldStatus = subscription.getStatus();
        String oldCode = subscription.getPlan().getPlanCode();
        if (request.planCode() != null && !request.planCode().isBlank()) subscription.changePlanNow(requireActivePlan(request.planCode(), subscription.getAudience()), Instant.now());
        subscription.setAdminDetails(request.status(), request.endsAt());
        record(subscription, actorId, "ADMIN_UPDATED", oldStatus.name(), subscription.getStatus().name(), oldCode,
            subscription.getPlan().getPlanCode(), "Updated by platform administrator");
        return SubscriptionResponse.from(subscriptions.save(subscription));
    }

    private java.util.Optional<Subscription> findCurrent(UUID ownerId, SubscriptionAudience audience) {
        Subscription subscription = subscriptions.findFirstByOwnerIdAndAudienceAndStatusInOrderByCreatedAtDesc(ownerId, audience, CURRENT_STATUSES)
            .orElse(null);
        if (subscription == null) return java.util.Optional.empty();

        Instant now = Instant.now();
        if (subscription.getPendingPlan() != null && subscription.getChangeEffectiveAt() != null
                && !subscription.getChangeEffectiveAt().isAfter(now)) {
            SubscriptionPlan oldPlan = subscription.getPlan();
            SubscriptionPlan nextPlan = subscription.getPendingPlan();
            charge(subscription, nextPlan, "scheduled plan change");
            subscription.applyPendingChange(now);
            record(subscription, ownerId, "DOWNGRADED", subscription.getStatus().name(),
                subscription.getStatus().name(), oldPlan.getPlanCode(), nextPlan.getPlanCode(), "Scheduled plan change applied");
            subscriptions.save(subscription);
        }

        if (subscription.getEndsAt().isAfter(now)) return java.util.Optional.of(subscription);
        subscription.setAdminStatus(SubscriptionStatus.EXPIRED);
        subscriptions.save(subscription);
        return java.util.Optional.empty();
    }

    private Subscription requireCurrent(UUID ownerId, SubscriptionAudience audience) {
        return findCurrent(ownerId, audience).orElseThrow(() -> notFound("No current subscription was found"));
    }

    private SubscriptionPlan requireActivePlan(String planCode, SubscriptionAudience audience) {
        SubscriptionPlan plan = plans.findByPlanCodeIgnoreCase(planCode.trim())
            .orElseThrow(() -> notFound("Plan was not found"));
        if (!plan.isActive() || plan.getAudience() != audience) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Plan is not available for this account");
        return plan;
    }

    private SubscriptionPlan requirePlan(UUID id) {
        return plans.findById(id).orElseThrow(() -> notFound("Plan was not found"));
    }

    private void charge(Subscription subscription, SubscriptionPlan plan, String description) {
        if (plan.getPrice().signum() == 0) return;
        PaymentGateway.PaymentResult result = payments.charge(subscription.getOwnerId(), plan.getPrice(), plan.getCurrency(), description);
        if (!result.approved()) throw new ResponseStatusException(HttpStatus.PAYMENT_REQUIRED, "Payment was declined");
        invoices.save(new Invoice(subscription.getId(), subscription.getOwnerId(), plan.getPlanCode(), plan.getPrice(), plan.getCurrency(), result.reference()));
    }

    private void record(Subscription subscription, UUID actorId, String action, String fromStatus, String toStatus,
            String fromPlan, String toPlan, String details) {
        history.save(new SubscriptionHistory(subscription.getId(), actorId, action, fromStatus, toStatus, fromPlan, toPlan, details));
    }

    private ResponseStatusException notFound(String message) { return new ResponseStatusException(HttpStatus.NOT_FOUND, message); }
}
