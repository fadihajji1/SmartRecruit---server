package com.smartrecruit.recruitment.api;

import com.smartrecruit.recruitment.domain.*;
import com.smartrecruit.recruitment.service.RecruitmentService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.*;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
public class RecruitmentController {
    private final RecruitmentService service;
    public RecruitmentController(RecruitmentService service) { this.service=service; }
    private UUID uid(Jwt jwt) { return UUID.fromString(jwt.getClaimAsString("user_id")); }
    public record ApplyRequest(UUID jobId,UUID candidateCvId,@Size(max=5000) String message) {}
    public record StatusRequest(@NotNull Application.Status status) {}
    public record BusinessRequest(@NotBlank String name,String description,String website,String industry,String address,String city,String country) {}

    @GetMapping("/api/v1/jobs") public Page<Job> jobs(@RequestParam(required=false) String q,@RequestParam(required=false) String location,
        @RequestParam(required=false) String contractType,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size) {
        return service.publicJobs(q,location,contractType,page,size);
    }
    @GetMapping("/api/v1/jobs/{id}") public Job job(@PathVariable UUID id) { return service.publicJob(id); }

    @GetMapping("/api/v1/recruiter/businesses") @PreAuthorize("hasRole('RECRUITER')")
    public List<Business> businesses(@AuthenticationPrincipal Jwt jwt) { return service.recruiterBusinesses(uid(jwt)); }
    @PostMapping("/api/v1/recruiter/businesses") @PreAuthorize("hasRole('RECRUITER')") @ResponseStatus(HttpStatus.CREATED)
    public Business createBusiness(@Valid @RequestBody BusinessRequest d,@AuthenticationPrincipal Jwt jwt) { return service.createBusiness(toBusiness(d),uid(jwt)); }
    @GetMapping("/api/v1/recruiter/businesses/{id}") @PreAuthorize("hasRole('RECRUITER')")
    public Business business(@PathVariable UUID id,@AuthenticationPrincipal Jwt jwt) { return service.business(id,uid(jwt)); }
    @PatchMapping("/api/v1/recruiter/businesses/{id}") @PreAuthorize("hasRole('RECRUITER')")
    public Business updateBusiness(@PathVariable UUID id,@Valid @RequestBody BusinessRequest d,@AuthenticationPrincipal Jwt jwt) { return service.updateBusiness(id,toBusiness(d),uid(jwt)); }
    @DeleteMapping("/api/v1/recruiter/businesses/{id}") @PreAuthorize("hasRole('RECRUITER')") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivateOwnBusiness(@PathVariable UUID id,@AuthenticationPrincipal Jwt jwt) { service.deactivateBusiness(id,uid(jwt)); }
    @GetMapping("/api/v1/admin/businesses") @PreAuthorize("hasRole('PLATFORM_ADMINISTRATOR')") public List<Business> allBusinesses() { return service.allBusinesses(); }
    @PostMapping("/api/v1/admin/businesses") @PreAuthorize("hasRole('PLATFORM_ADMINISTRATOR')") @ResponseStatus(HttpStatus.CREATED)
    public Business adminCreateBusiness(@Valid @RequestBody Business b) { return service.adminCreateBusiness(b); }
    @GetMapping("/api/v1/admin/businesses/{id}") @PreAuthorize("hasRole('PLATFORM_ADMINISTRATOR')") public Business adminBusiness(@PathVariable UUID id) { return service.adminBusiness(id); }
    @PatchMapping("/api/v1/admin/businesses/{id}") @PreAuthorize("hasRole('PLATFORM_ADMINISTRATOR')")
    public Business adminUpdateBusiness(@PathVariable UUID id,@RequestBody Business b) { return service.adminUpdateBusiness(id,b); }
    @DeleteMapping("/api/v1/admin/businesses/{id}") @PreAuthorize("hasRole('PLATFORM_ADMINISTRATOR')") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivateBusiness(@PathVariable UUID id) { service.deactivateBusiness(id); }

    @GetMapping("/api/v1/recruiter/jobs") @PreAuthorize("hasRole('RECRUITER')") public List<Job> recruiterJobs(@AuthenticationPrincipal Jwt jwt) { return service.recruiterJobs(uid(jwt)); }
    @PostMapping("/api/v1/recruiter/jobs") @PreAuthorize("hasRole('RECRUITER')") @ResponseStatus(HttpStatus.CREATED)
    public Job create(@Valid @RequestBody Job j,@AuthenticationPrincipal Jwt jwt) { return service.createJob(j,uid(jwt)); }
    @GetMapping("/api/v1/recruiter/jobs/{id}") @PreAuthorize("hasRole('RECRUITER')") public Job recruiterJob(@PathVariable UUID id,@AuthenticationPrincipal Jwt jwt) { return service.recruiterJob(id,uid(jwt)); }
    @PatchMapping("/api/v1/recruiter/jobs/{id}") @PreAuthorize("hasRole('RECRUITER')") public Job update(@PathVariable UUID id,@Valid @RequestBody Job j,@AuthenticationPrincipal Jwt jwt) { return service.updateJob(id,j,uid(jwt)); }
    @PostMapping("/api/v1/recruiter/jobs/{id}/close") @PreAuthorize("hasRole('RECRUITER')") public Job close(@PathVariable UUID id,@AuthenticationPrincipal Jwt jwt) { return service.transition(id,uid(jwt),Job.Status.CLOSED); }
    @PostMapping("/api/v1/recruiter/jobs/{id}/publish") @PreAuthorize("hasRole('RECRUITER')") public Job publish(@PathVariable UUID id,@AuthenticationPrincipal Jwt jwt) { return service.transition(id,uid(jwt),Job.Status.PUBLISHED); }
    @PostMapping("/api/v1/recruiter/jobs/{id}/reopen") @PreAuthorize("hasRole('RECRUITER')") public Job reopen(@PathVariable UUID id,@AuthenticationPrincipal Jwt jwt) { return service.transition(id,uid(jwt),Job.Status.PUBLISHED); }
    @DeleteMapping("/api/v1/recruiter/jobs/{id}") @PreAuthorize("hasRole('RECRUITER')") public Job delete(@PathVariable UUID id,@AuthenticationPrincipal Jwt jwt) { return service.transition(id,uid(jwt),Job.Status.DELETED); }

    @PostMapping("/api/v1/applications") @PreAuthorize("hasRole('CANDIDATE')") @ResponseStatus(HttpStatus.CREATED)
    public Application apply(@Valid @RequestBody ApplyRequest r,@AuthenticationPrincipal Jwt jwt) { return service.apply(r.jobId(),r.candidateCvId(),r.message(),uid(jwt)); }
    @GetMapping("/api/v1/candidate/applications") @PreAuthorize("hasRole('CANDIDATE')") public List<Application> candidate(@AuthenticationPrincipal Jwt jwt) { return service.candidateApplications(uid(jwt)); }
    @GetMapping("/api/v1/candidate/applications/{id}") @PreAuthorize("hasRole('CANDIDATE')") public Application candidateApplication(@PathVariable UUID id,@AuthenticationPrincipal Jwt jwt) { return service.candidateApplication(id,uid(jwt)); }
    @GetMapping("/api/v1/recruiter/applications") @PreAuthorize("hasRole('RECRUITER')") public List<Application> recruiter(@AuthenticationPrincipal Jwt jwt) { return service.recruiterApplications(uid(jwt)); }
    @GetMapping("/api/v1/recruiter/applications/{id}") @PreAuthorize("hasRole('RECRUITER')") public Application recruiterApplication(@PathVariable UUID id,@AuthenticationPrincipal Jwt jwt) { return service.recruiterApplication(id,uid(jwt)); }
    @PatchMapping("/api/v1/recruiter/applications/{id}/status") @PreAuthorize("hasRole('RECRUITER')") public Application status(@PathVariable UUID id,@Valid @RequestBody StatusRequest r,@AuthenticationPrincipal Jwt jwt) { return service.updateStatus(id,r.status(),uid(jwt)); }

    private Business toBusiness(BusinessRequest d) { Business b=new Business();b.name=d.name();b.description=d.description();b.website=d.website();b.industry=d.industry();b.address=d.address();b.city=d.city();b.country=d.country();return b; }
}
