package com.smartrecruit.subscription.api;

import com.smartrecruit.subscription.domain.Subscription;
import com.smartrecruit.subscription.repo.SubscriptionRepository;
import java.util.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/subscriptions") public class SubscriptionController {
 private final SubscriptionRepository repo;
 public SubscriptionController(SubscriptionRepository repo){
this.repo=repo;
}
 @GetMapping public List<Subscription> all(){
return repo.findAll();
}
 @GetMapping("/{id}") public Subscription one(@PathVariable UUID id){
return repo.findById(id).orElseThrow();
}
 @PostMapping public Subscription create(@RequestBody Subscription value){
value.id=null;
return repo.save(value);
}
 @PatchMapping("/{id}") public Subscription update(@PathVariable UUID id,@RequestBody Subscription value){
value.id=id;
return repo.save(value);
}
 @DeleteMapping("/{id}") public void delete(@PathVariable UUID id){
repo.deleteById(id);
}
 }

