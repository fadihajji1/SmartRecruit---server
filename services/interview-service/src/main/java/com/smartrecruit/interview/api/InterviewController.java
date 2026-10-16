package com.smartrecruit.interview.api;

import com.smartrecruit.interview.domain.Interview;
import com.smartrecruit.interview.repo.InterviewRepository;
import java.util.*;
import java.net.URI;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1") public class InterviewController {
 private final InterviewRepository repo;
 public InterviewController(InterviewRepository repo){
this.repo=repo;
}
 @GetMapping("/recruiter/interviews") @PreAuthorize("hasRole('RECRUITER')") public List<Interview> all(@AuthenticationPrincipal Jwt jwt){
return repo.findByRecruiterId(userId(jwt));
}
 @GetMapping("/recruiter/interviews/{id}") @PreAuthorize("hasRole('RECRUITER')") public Interview one(@PathVariable UUID id,@AuthenticationPrincipal Jwt jwt){
return owned(id,userId(jwt));
}
 @PostMapping("/recruiter/interviews") @PreAuthorize("hasRole('RECRUITER')") public Interview create(@RequestBody Interview value,@AuthenticationPrincipal Jwt jwt){
if(value.candidateId==null||value.applicationId==null||value.scheduledAt==null||!value.scheduledAt.isAfter(Instant.now()))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Candidate, application and a future scheduledAt are required");
validateMeetingUrl(value.externalMeetingUrl);
value.id=null;value.recruiterId=userId(jwt);value.status="SCHEDULED";
return repo.save(value);
}
 @PatchMapping("/recruiter/interviews/{id}") @PreAuthorize("hasRole('RECRUITER')") public Interview update(@PathVariable UUID id,@RequestBody Interview value,@AuthenticationPrincipal Jwt jwt){
Interview current=owned(id,userId(jwt));
if(value.scheduledAt!=null){if(!value.scheduledAt.isAfter(Instant.now()))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"scheduledAt must be in the future");current.scheduledAt=value.scheduledAt;}
if(value.externalMeetingUrl!=null){validateMeetingUrl(value.externalMeetingUrl);current.externalMeetingUrl=value.externalMeetingUrl;}
if(value.status!=null){if(!Set.of("SCHEDULED","CANCELLED","COMPLETED").contains(value.status))throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Invalid interview status");current.status=value.status;}
return repo.save(current);
 }
 @DeleteMapping("/recruiter/interviews/{id}") @PreAuthorize("hasRole('RECRUITER')") public void delete(@PathVariable UUID id,@AuthenticationPrincipal Jwt jwt){
repo.delete(owned(id,userId(jwt)));
 }
 @GetMapping("/candidate/interviews") @PreAuthorize("hasRole('CANDIDATE')") public List<Interview> candidateInterviews(@AuthenticationPrincipal Jwt jwt){
 return repo.findByCandidateId(userId(jwt));
 }
 @GetMapping("/candidate/interviews/{id}") @PreAuthorize("hasRole('CANDIDATE')") public Interview candidateInterview(@PathVariable UUID id,@AuthenticationPrincipal Jwt jwt){
 return repo.findByIdAndCandidateId(id,userId(jwt)).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
 }
 private Interview owned(UUID id,UUID recruiter){return repo.findByIdAndRecruiterId(id,recruiter).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));}
 private UUID userId(Jwt jwt){try{return UUID.fromString(jwt.getClaimAsString("user_id"));}catch(RuntimeException e){throw new AccessDeniedException("Token has no valid user_id",e);}}
 private void validateMeetingUrl(String value){
  if(value==null||value.isBlank())return;
  try{URI uri=URI.create(value);if(!"https".equalsIgnoreCase(uri.getScheme())||uri.getHost()==null)throw new IllegalArgumentException();}
  catch(IllegalArgumentException e){throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Meeting URL must be an absolute HTTPS URL");}
 }
 }

