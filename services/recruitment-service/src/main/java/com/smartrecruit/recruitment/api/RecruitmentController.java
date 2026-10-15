package com.smartrecruit.recruitment.api;

import com.smartrecruit.recruitment.domain.*;
import com.smartrecruit.recruitment.service.RecruitmentService;
import java.util.*;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController public class RecruitmentController {
private final RecruitmentService service;
public RecruitmentController(RecruitmentService s){
service=s;
}
private UUID uid(Jwt j){
return UUID.fromString(j.getClaimAsString("user_id"));
}
public record ApplyRequest(UUID jobId,UUID candidateCvId){
}
public record StatusRequest(Application.Status status){
}
@GetMapping("/api/v1/jobs")public Page<Job> jobs(@RequestParam(required=false)String q,@RequestParam(defaultValue="0")int page,@RequestParam(defaultValue="20")int size){
return service.publicJobs(q,page,size);
}
@GetMapping("/api/v1/jobs/{id}")public Job job(@PathVariable UUID id){
return service.publicJob(id);
}
@PostMapping("/api/v1/recruiter/jobs")@PreAuthorize("hasRole('RECRUITER')")public Job create(@RequestBody Job j,@AuthenticationPrincipal Jwt jwt){
return service.createJob(j,uid(jwt));
}
@PatchMapping("/api/v1/recruiter/jobs/{id}")@PreAuthorize("hasRole('RECRUITER')")public Job update(@PathVariable UUID id,@RequestBody Job j,@AuthenticationPrincipal Jwt jwt){
return service.updateJob(id,j,uid(jwt));
}
@PostMapping("/api/v1/applications")@PreAuthorize("hasRole('CANDIDATE')")public Application apply(@RequestBody ApplyRequest r,@AuthenticationPrincipal Jwt jwt){
return service.apply(r.jobId(),r.candidateCvId(),uid(jwt));
}
@GetMapping("/api/v1/candidate/applications")@PreAuthorize("hasRole('CANDIDATE')")public List<Application> candidate(@AuthenticationPrincipal Jwt jwt){
return service.candidateApplications(uid(jwt));
}
@GetMapping("/api/v1/recruiter/applications")@PreAuthorize("hasRole('RECRUITER')")public List<Application> recruiter(@AuthenticationPrincipal Jwt jwt){
return service.recruiterApplications(uid(jwt));
}
@PatchMapping("/api/v1/recruiter/applications/{id}/status")@PreAuthorize("hasRole('RECRUITER')")public Application status(@PathVariable UUID id,@RequestBody StatusRequest r,@AuthenticationPrincipal Jwt jwt){
return service.updateStatus(id,r.status(),uid(jwt));
}
}

