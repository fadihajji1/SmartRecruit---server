package com.smartrecruit.analytics.api;

import com.smartrecruit.analytics.domain.Metric;
import com.smartrecruit.analytics.repo.MetricRepository;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/recruiter/analytics") public class MetricController {
 private final MetricRepository repo;
 public MetricController(MetricRepository repo){
this.repo=repo;
}
 @GetMapping @PreAuthorize("hasRole('RECRUITER')") public List<Metric> all(@AuthenticationPrincipal Jwt jwt){
return repo.findByRecruiterIdOrderByMeasuredAtDesc(userId(jwt));
}
 @GetMapping("/{id}") @PreAuthorize("hasRole('RECRUITER')") public Metric one(@PathVariable UUID id,@AuthenticationPrincipal Jwt jwt){
return repo.findByIdAndRecruiterId(id,userId(jwt)).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
}
 private UUID userId(Jwt jwt){try{return UUID.fromString(jwt.getClaimAsString("user_id"));}catch(RuntimeException e){throw new AccessDeniedException("Token has no valid user_id",e);}}
 }

