package com.smartrecruit.analytics.api;

import com.smartrecruit.analytics.domain.Metric;
import com.smartrecruit.analytics.repo.MetricRepository;
import java.util.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/recruiter/analytics") public class MetricController {
 private final MetricRepository repo;
 public MetricController(MetricRepository repo){
this.repo=repo;
}
 @GetMapping public List<Metric> all(){
return repo.findAll();
}
 @GetMapping("/{id}") public Metric one(@PathVariable UUID id){
return repo.findById(id).orElseThrow();
}
 @PostMapping public Metric create(@RequestBody Metric value){
value.id=null;
return repo.save(value);
}
 @PatchMapping("/{id}") public Metric update(@PathVariable UUID id,@RequestBody Metric value){
value.id=id;
return repo.save(value);
}
 @DeleteMapping("/{id}") public void delete(@PathVariable UUID id){
repo.deleteById(id);
}
 }

