package com.smartrecruit.subscription.api;

import com.smartrecruit.subscription.domain.Subscription;
import com.smartrecruit.subscription.repo.SubscriptionRepository;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/subscriptions") public class SubscriptionController {
 private final SubscriptionRepository repo;
 public SubscriptionController(SubscriptionRepository repo){
this.repo=repo;
}
 @GetMapping @PreAuthorize("hasAnyRole('CANDIDATE','RECRUITER','PLATFORM_ADMINISTRATOR')") public List<Subscription> all(@AuthenticationPrincipal Jwt jwt,Authentication authentication){
return isAdmin(authentication)?repo.findAll():repo.findByOwnerId(userId(jwt));
}
 @GetMapping("/{id}") @PreAuthorize("hasAnyRole('CANDIDATE','RECRUITER','PLATFORM_ADMINISTRATOR')") public Subscription one(@PathVariable UUID id,@AuthenticationPrincipal Jwt jwt,Authentication authentication){
return isAdmin(authentication)?repo.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND)):repo.findByIdAndOwnerId(id,userId(jwt)).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
}
 @PostMapping @PreAuthorize("hasRole('PLATFORM_ADMINISTRATOR')") public Subscription create(@RequestBody Subscription value){
value.id=null;
return repo.save(value);
}
 @PatchMapping("/{id}") @PreAuthorize("hasRole('PLATFORM_ADMINISTRATOR')") public Subscription update(@PathVariable UUID id,@RequestBody Subscription value){
if(!repo.existsById(id))throw new ResponseStatusException(HttpStatus.NOT_FOUND);
value.id=id;
return repo.save(value);
}
 @DeleteMapping("/{id}") @PreAuthorize("hasRole('PLATFORM_ADMINISTRATOR')") public void delete(@PathVariable UUID id){
if(!repo.existsById(id))throw new ResponseStatusException(HttpStatus.NOT_FOUND);
repo.deleteById(id);
}
 private UUID userId(Jwt jwt){try{return UUID.fromString(jwt.getClaimAsString("user_id"));}catch(RuntimeException e){throw new AccessDeniedException("Token has no valid user_id",e);}}
 private boolean isAdmin(Authentication authentication){return authentication.getAuthorities().stream().anyMatch(a->a.getAuthority().equals("ROLE_PLATFORM_ADMINISTRATOR"));}
 }

