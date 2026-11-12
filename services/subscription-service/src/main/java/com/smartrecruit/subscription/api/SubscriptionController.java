package com.smartrecruit.subscription.api;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import com.smartrecruit.subscription.domain.SubscriptionAudience;
import com.smartrecruit.subscription.dto.AdminSubscriptionUpdateRequest;
import com.smartrecruit.subscription.dto.InvoiceResponse;
import com.smartrecruit.subscription.dto.PlanRequest;
import com.smartrecruit.subscription.dto.PlanResponse;
import com.smartrecruit.subscription.dto.SubscribeRequest;
import com.smartrecruit.subscription.dto.SubscriptionHistoryResponse;
import com.smartrecruit.subscription.dto.SubscriptionResponse;
import com.smartrecruit.subscription.service.SubscriptionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1")
@Validated
@Tag(name = "Subscriptions", description = "Plans, subscriptions, billing history and plan changes")
public class SubscriptionController {
    private final SubscriptionService service;
    public SubscriptionController(SubscriptionService service) { this.service = service; }

    @GetMapping("/subscriptions/plans")
    @Operation(summary = "List active plans", description = "Public plan catalog. Optionally filter by CANDIDATE or RECRUITER.")
    public List<PlanResponse> plans(@RequestParam(required = false) SubscriptionAudience audience) { return service.listPlans(audience); }

    @GetMapping("/candidate/subscription") @PreAuthorize("hasRole('CANDIDATE')") @SecurityRequirement(name = "bearerAuth")
    public SubscriptionResponse candidateCurrent(@AuthenticationPrincipal Jwt jwt) { return service.current(actorId(jwt), SubscriptionAudience.CANDIDATE); }
    @PostMapping("/candidate/subscription") @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasRole('CANDIDATE')") @SecurityRequirement(name = "bearerAuth")
    public SubscriptionResponse candidateSubscribe(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody SubscribeRequest request) { return service.subscribe(actorId(jwt), SubscriptionAudience.CANDIDATE, request); }
    @PostMapping("/candidate/subscription/upgrade") @PreAuthorize("hasRole('CANDIDATE')") @SecurityRequirement(name = "bearerAuth")
    public SubscriptionResponse candidateUpgrade(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody SubscribeRequest request) { return service.upgrade(actorId(jwt), SubscriptionAudience.CANDIDATE, request); }
    @PostMapping("/candidate/subscription/downgrade") @PreAuthorize("hasRole('CANDIDATE')") @SecurityRequirement(name = "bearerAuth")
    public SubscriptionResponse candidateDowngrade(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody SubscribeRequest request) { return service.downgrade(actorId(jwt), SubscriptionAudience.CANDIDATE, request); }
    @PostMapping("/candidate/subscription/renew") @PreAuthorize("hasRole('CANDIDATE')") @SecurityRequirement(name = "bearerAuth")
    public SubscriptionResponse candidateRenew(@AuthenticationPrincipal Jwt jwt) { return service.renew(actorId(jwt), SubscriptionAudience.CANDIDATE); }
    @PostMapping("/candidate/subscription/cancel") @PreAuthorize("hasRole('CANDIDATE')") @SecurityRequirement(name = "bearerAuth")
    public SubscriptionResponse candidateCancel(@AuthenticationPrincipal Jwt jwt) { return service.cancel(actorId(jwt), SubscriptionAudience.CANDIDATE); }
    @GetMapping("/candidate/subscription/history") @PreAuthorize("hasRole('CANDIDATE')") @SecurityRequirement(name = "bearerAuth")
    public Page<SubscriptionHistoryResponse> candidateHistory(@AuthenticationPrincipal Jwt jwt, @RequestParam(defaultValue = "0") @Min(0) int page, @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) { return service.history(actorId(jwt), SubscriptionAudience.CANDIDATE, PageRequest.of(page, size)); }
    @GetMapping("/candidate/subscription/invoices") @PreAuthorize("hasRole('CANDIDATE')") @SecurityRequirement(name = "bearerAuth")
    public Page<InvoiceResponse> candidateInvoices(@AuthenticationPrincipal Jwt jwt, @RequestParam(defaultValue = "0") @Min(0) int page, @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) { return service.invoices(actorId(jwt), PageRequest.of(page, size)); }

    @GetMapping("/recruiter/subscription") @PreAuthorize("hasRole('RECRUITER')") @SecurityRequirement(name = "bearerAuth")
    public SubscriptionResponse recruiterCurrent(@AuthenticationPrincipal Jwt jwt) { return service.current(actorId(jwt), SubscriptionAudience.RECRUITER); }
    @PostMapping("/recruiter/subscription") @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasRole('RECRUITER')") @SecurityRequirement(name = "bearerAuth")
    public SubscriptionResponse recruiterSubscribe(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody SubscribeRequest request) { return service.subscribe(actorId(jwt), SubscriptionAudience.RECRUITER, request); }
    @PostMapping("/recruiter/subscription/upgrade") @PreAuthorize("hasRole('RECRUITER')") @SecurityRequirement(name = "bearerAuth")
    public SubscriptionResponse recruiterUpgrade(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody SubscribeRequest request) { return service.upgrade(actorId(jwt), SubscriptionAudience.RECRUITER, request); }
    @PostMapping("/recruiter/subscription/downgrade") @PreAuthorize("hasRole('RECRUITER')") @SecurityRequirement(name = "bearerAuth")
    public SubscriptionResponse recruiterDowngrade(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody SubscribeRequest request) { return service.downgrade(actorId(jwt), SubscriptionAudience.RECRUITER, request); }
    @PostMapping("/recruiter/subscription/renew") @PreAuthorize("hasRole('RECRUITER')") @SecurityRequirement(name = "bearerAuth")
    public SubscriptionResponse recruiterRenew(@AuthenticationPrincipal Jwt jwt) { return service.renew(actorId(jwt), SubscriptionAudience.RECRUITER); }
    @PostMapping("/recruiter/subscription/cancel") @PreAuthorize("hasRole('RECRUITER')") @SecurityRequirement(name = "bearerAuth")
    public SubscriptionResponse recruiterCancel(@AuthenticationPrincipal Jwt jwt) { return service.cancel(actorId(jwt), SubscriptionAudience.RECRUITER); }
    @GetMapping("/recruiter/subscription/history") @PreAuthorize("hasRole('RECRUITER')") @SecurityRequirement(name = "bearerAuth")
    public Page<SubscriptionHistoryResponse> recruiterHistory(@AuthenticationPrincipal Jwt jwt, @RequestParam(defaultValue = "0") @Min(0) int page, @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) { return service.history(actorId(jwt), SubscriptionAudience.RECRUITER, PageRequest.of(page, size)); }
    @GetMapping("/recruiter/subscription/invoices") @PreAuthorize("hasRole('RECRUITER')") @SecurityRequirement(name = "bearerAuth")
    public Page<InvoiceResponse> recruiterInvoices(@AuthenticationPrincipal Jwt jwt, @RequestParam(defaultValue = "0") @Min(0) int page, @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) { return service.invoices(actorId(jwt), PageRequest.of(page, size)); }

    @GetMapping("/admin/subscription/plans") @PreAuthorize("hasRole('PLATFORM_ADMINISTRATOR')") @SecurityRequirement(name = "bearerAuth")
    public List<PlanResponse> adminPlans() { return service.listAllPlans(); }
    @PostMapping("/admin/subscription/plans") @ResponseStatus(HttpStatus.CREATED) @PreAuthorize("hasRole('PLATFORM_ADMINISTRATOR')") @SecurityRequirement(name = "bearerAuth")
    public PlanResponse createPlan(@Valid @RequestBody PlanRequest request) { return service.createPlan(request); }
    @PatchMapping("/admin/subscription/plans/{planId}") @PreAuthorize("hasRole('PLATFORM_ADMINISTRATOR')") @SecurityRequirement(name = "bearerAuth")
    public PlanResponse updatePlan(@PathVariable UUID planId, @Valid @RequestBody PlanRequest request) { return service.updatePlan(planId, request); }
    @GetMapping("/admin/subscription/company-subscriptions") @PreAuthorize("hasRole('PLATFORM_ADMINISTRATOR')") @SecurityRequirement(name = "bearerAuth")
    public Page<SubscriptionResponse> companySubscriptions(@RequestParam(defaultValue = "0") @Min(0) int page, @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) { return service.companySubscriptions(PageRequest.of(page, size)); }
    @PatchMapping("/admin/subscription/company-subscriptions/{subscriptionId}") @PreAuthorize("hasRole('PLATFORM_ADMINISTRATOR')") @SecurityRequirement(name = "bearerAuth")
    public SubscriptionResponse updateCompanySubscription(@PathVariable UUID subscriptionId, @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody AdminSubscriptionUpdateRequest request) { return service.adminUpdate(subscriptionId, actorId(jwt), request); }

    private UUID actorId(Jwt jwt) {
        String value = jwt.getClaimAsString("user_id");
        if (value == null || value.isBlank()) throw new org.springframework.security.access.AccessDeniedException("Token has no user id");
        return UUID.fromString(value);
    }
}
