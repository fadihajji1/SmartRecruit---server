package com.smartrecruit.recruitment.api;

import com.smartrecruit.recruitment.domain.Application;
import com.smartrecruit.recruitment.domain.Business;
import com.smartrecruit.recruitment.domain.Job;
import com.smartrecruit.recruitment.service.RecruitmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Top-level recruitment HTTP surface. Route groups:
 *   /api/v1/jobs                     public job catalogue
 *   /api/v1/applications             candidate applies
 *   /api/v1/candidate/applications   candidate reads own applications
 *   /api/v1/recruiter/*              recruiter manages their businesses, jobs, applications
 *   /api/v1/admin/*                  platform administrator moderation + stats
 *
 * All methods are thin: they adapt HTTP in/out and delegate every rule to {@link RecruitmentService}.
 */
@RestController
@RequestMapping("/api/v1")
@Tag(name = "Recruitment", description = "Jobs, businesses and applications")
public class RecruitmentController {

    private final RecruitmentService service;

    public RecruitmentController(RecruitmentService service) {
        this.service = service;
    }

    // --------------------------------------------------------------------
    // Request DTOs
    // --------------------------------------------------------------------
    public record ApplyRequest(UUID jobId, UUID candidateCvId, @Size(max = 5000) String message) {}
    public record StatusRequest(@NotNull Application.Status status) {}
    public record BusinessRequest(
            @NotBlank String name,
            String description,
            String website,
            String industry,
            String address,
            String city,
            String country) {}

    // ====================================================================
    // Public job catalogue (unauthenticated)
    // ====================================================================
    @GetMapping("/jobs")
    @Operation(summary = "Search published job offers")
    public PageResponse<Job> jobs(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String contractType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return PageResponse.of(service.publicJobs(q, location, contractType, page, size));
    }

    @GetMapping("/jobs/{id}")
    @Operation(summary = "Fetch a single published job offer")
    public Job job(@PathVariable UUID id) {
        return service.publicJob(id);
    }

    // ====================================================================
    // Recruiter — businesses
    // ====================================================================
    @GetMapping("/recruiter/businesses")
    @PreAuthorize("hasRole('RECRUITER')") @SecurityRequirement(name = "bearerAuth")
    public List<Business> businesses(@AuthenticationPrincipal Jwt jwt) {
        return service.recruiterBusinesses(uid(jwt));
    }

    @PostMapping("/recruiter/businesses")
    @PreAuthorize("hasRole('RECRUITER')") @SecurityRequirement(name = "bearerAuth")
    @ResponseStatus(HttpStatus.CREATED)
    public Business createBusiness(@Valid @RequestBody BusinessRequest request,
                                   @AuthenticationPrincipal Jwt jwt) {
        return service.createBusiness(toBusiness(request), uid(jwt));
    }

    @GetMapping("/recruiter/businesses/{id}")
    @PreAuthorize("hasRole('RECRUITER')") @SecurityRequirement(name = "bearerAuth")
    public Business business(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        return service.business(id, uid(jwt));
    }

    @PatchMapping("/recruiter/businesses/{id}")
    @PreAuthorize("hasRole('RECRUITER')") @SecurityRequirement(name = "bearerAuth")
    public Business updateBusiness(@PathVariable UUID id,
                                   @Valid @RequestBody BusinessRequest request,
                                   @AuthenticationPrincipal Jwt jwt) {
        return service.updateBusiness(id, toBusiness(request), uid(jwt));
    }

    @DeleteMapping("/recruiter/businesses/{id}")
    @PreAuthorize("hasRole('RECRUITER')") @SecurityRequirement(name = "bearerAuth")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivateOwnBusiness(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        service.deactivateBusiness(id, uid(jwt));
    }

    // ====================================================================
    // Recruiter — jobs
    // ====================================================================
    @GetMapping("/recruiter/jobs")
    @PreAuthorize("hasRole('RECRUITER')") @SecurityRequirement(name = "bearerAuth")
    public List<Job> recruiterJobs(@AuthenticationPrincipal Jwt jwt) {
        return service.recruiterJobs(uid(jwt));
    }

    @PostMapping("/recruiter/jobs")
    @PreAuthorize("hasRole('RECRUITER')") @SecurityRequirement(name = "bearerAuth")
    @ResponseStatus(HttpStatus.CREATED)
    public Job create(@Valid @RequestBody Job job, @AuthenticationPrincipal Jwt jwt) {
        return service.createJob(job, uid(jwt));
    }

    @GetMapping("/recruiter/jobs/{id}")
    @PreAuthorize("hasRole('RECRUITER')") @SecurityRequirement(name = "bearerAuth")
    public Job recruiterJob(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        return service.recruiterJob(id, uid(jwt));
    }

    @PatchMapping("/recruiter/jobs/{id}")
    @PreAuthorize("hasRole('RECRUITER')") @SecurityRequirement(name = "bearerAuth")
    public Job update(@PathVariable UUID id, @Valid @RequestBody Job job,
                      @AuthenticationPrincipal Jwt jwt) {
        return service.updateJob(id, job, uid(jwt));
    }

    @PostMapping("/recruiter/jobs/{id}/publish")
    @PreAuthorize("hasRole('RECRUITER')") @SecurityRequirement(name = "bearerAuth")
    public Job publish(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        return service.transition(id, uid(jwt), Job.Status.PUBLISHED);
    }

    @PostMapping("/recruiter/jobs/{id}/close")
    @PreAuthorize("hasRole('RECRUITER')") @SecurityRequirement(name = "bearerAuth")
    public Job close(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        return service.transition(id, uid(jwt), Job.Status.CLOSED);
    }

    @PostMapping("/recruiter/jobs/{id}/reopen")
    @PreAuthorize("hasRole('RECRUITER')") @SecurityRequirement(name = "bearerAuth")
    public Job reopen(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        return service.transition(id, uid(jwt), Job.Status.PUBLISHED);
    }

    @DeleteMapping("/recruiter/jobs/{id}")
    @PreAuthorize("hasRole('RECRUITER')") @SecurityRequirement(name = "bearerAuth")
    public Job delete(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        return service.transition(id, uid(jwt), Job.Status.DELETED);
    }

    // ====================================================================
    // Applications
    // ====================================================================
    @PostMapping("/applications")
    @PreAuthorize("hasRole('CANDIDATE')") @SecurityRequirement(name = "bearerAuth")
    @ResponseStatus(HttpStatus.CREATED)
    public Application apply(@Valid @RequestBody ApplyRequest request,
                             @AuthenticationPrincipal Jwt jwt) {
        return service.apply(
                request.jobId(),
                request.candidateCvId(),
                request.message(),
                uid(jwt),
                jwt.getClaimAsString("name"),
                jwt.getSubject());
    }

    @GetMapping("/candidate/applications")
    @PreAuthorize("hasRole('CANDIDATE')") @SecurityRequirement(name = "bearerAuth")
    public List<Application> candidate(@AuthenticationPrincipal Jwt jwt) {
        return service.candidateApplications(uid(jwt));
    }

    @GetMapping("/candidate/applications/{id}")
    @PreAuthorize("hasRole('CANDIDATE')") @SecurityRequirement(name = "bearerAuth")
    public Application candidateApplication(@PathVariable UUID id,
                                            @AuthenticationPrincipal Jwt jwt) {
        return service.candidateApplication(id, uid(jwt));
    }

    @GetMapping("/recruiter/applications")
    @PreAuthorize("hasRole('RECRUITER')") @SecurityRequirement(name = "bearerAuth")
    public List<Application> recruiter(@AuthenticationPrincipal Jwt jwt) {
        return service.recruiterApplications(uid(jwt));
    }

    @GetMapping("/recruiter/applications/{id}")
    @PreAuthorize("hasRole('RECRUITER')") @SecurityRequirement(name = "bearerAuth")
    public Application recruiterApplication(@PathVariable UUID id,
                                            @AuthenticationPrincipal Jwt jwt) {
        return service.recruiterApplication(id, uid(jwt));
    }

    @PatchMapping("/recruiter/applications/{id}/status")
    @PreAuthorize("hasRole('RECRUITER')") @SecurityRequirement(name = "bearerAuth")
    public Application status(@PathVariable UUID id,
                              @Valid @RequestBody StatusRequest request,
                              @AuthenticationPrincipal Jwt jwt) {
        return service.updateStatus(id, request.status(), uid(jwt));
    }

    // ====================================================================
    // Platform administrator — businesses, jobs, stats
    // ====================================================================
    @GetMapping("/admin/businesses")
    @PreAuthorize("hasRole('PLATFORM_ADMINISTRATOR')") @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "List every business profile on the platform")
    public List<Business> allBusinesses() {
        return service.allBusinesses();
    }

    @PostMapping("/admin/businesses")
    @PreAuthorize("hasRole('PLATFORM_ADMINISTRATOR')") @SecurityRequirement(name = "bearerAuth")
    @ResponseStatus(HttpStatus.CREATED)
    public Business adminCreateBusiness(@Valid @RequestBody Business business) {
        return service.adminCreateBusiness(business);
    }

    @GetMapping("/admin/businesses/{id}")
    @PreAuthorize("hasRole('PLATFORM_ADMINISTRATOR')") @SecurityRequirement(name = "bearerAuth")
    public Business adminBusiness(@PathVariable UUID id) {
        return service.adminBusiness(id);
    }

    @PatchMapping("/admin/businesses/{id}")
    @PreAuthorize("hasRole('PLATFORM_ADMINISTRATOR')") @SecurityRequirement(name = "bearerAuth")
    public Business adminUpdateBusiness(@PathVariable UUID id, @RequestBody Business business) {
        return service.adminUpdateBusiness(id, business);
    }

    @DeleteMapping("/admin/businesses/{id}")
    @PreAuthorize("hasRole('PLATFORM_ADMINISTRATOR')") @SecurityRequirement(name = "bearerAuth")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivateBusiness(@PathVariable UUID id) {
        service.deactivateBusiness(id);
    }

    @GetMapping("/admin/jobs")
    @PreAuthorize("hasRole('PLATFORM_ADMINISTRATOR')") @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "List every job (admin moderation view)")
    public List<Job> adminJobs() {
        return service.allJobs();
    }

    @PostMapping("/admin/jobs/{id}/close")
    @PreAuthorize("hasRole('PLATFORM_ADMINISTRATOR')") @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Close a published job that violates platform rules")
    public Job adminClose(@PathVariable UUID id) {
        return service.adminClose(id);
    }

    @GetMapping("/admin/recruitment/stats")
    @PreAuthorize("hasRole('PLATFORM_ADMINISTRATOR')") @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Platform-wide counters: jobs by status, total applications, total businesses")
    public Map<String, Long> adminStats() {
        return service.adminStats();
    }

    // --------------------------------------------------------------------
    // Internal helpers
    // --------------------------------------------------------------------
    private static UUID uid(Jwt jwt) {
        return UUID.fromString(jwt.getClaimAsString("user_id"));
    }

    private static Business toBusiness(BusinessRequest request) {
        Business b = new Business();
        b.name = request.name();
        b.description = request.description();
        b.website = request.website();
        b.industry = request.industry();
        b.address = request.address();
        b.city = request.city();
        b.country = request.country();
        return b;
    }
}
