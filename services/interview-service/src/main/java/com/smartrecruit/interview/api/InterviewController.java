package com.smartrecruit.interview.api;

import com.smartrecruit.interview.domain.Interview;
import com.smartrecruit.interview.repo.InterviewRepository;
import java.util.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/v1/recruiter/interviews") public class InterviewController {
 private final InterviewRepository repo;
 public InterviewController(InterviewRepository repo){
this.repo=repo;
}
 @GetMapping public List<Interview> all(){
return repo.findAll();
}
 @GetMapping("/{id}") public Interview one(@PathVariable UUID id){
return repo.findById(id).orElseThrow();
}
 @PostMapping public Interview create(@RequestBody Interview value){
value.id=null;
return repo.save(value);
}
 @PatchMapping("/{id}") public Interview update(@PathVariable UUID id,@RequestBody Interview value){
value.id=id;
return repo.save(value);
}
 @DeleteMapping("/{id}") public void delete(@PathVariable UUID id){
repo.deleteById(id);
}
 }

