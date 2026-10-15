package com.smartrecruit.candidateai.api;

import com.smartrecruit.candidateai.domain.CandidateCv;
import com.smartrecruit.candidateai.service.CvService;
import java.util.*;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController @RequestMapping("/api/v1/candidate/cvs") @PreAuthorize("hasRole('CANDIDATE')") public class CvController {
 private final CvService service;
public CvController(CvService service){
this.service=service;
}
private UUID id(Jwt jwt){
return UUID.fromString(jwt.getClaimAsString("user_id"));
}
@GetMapping public List<CandidateCv> list(@AuthenticationPrincipal Jwt jwt){
return service.list(id(jwt));
}
@PostMapping(consumes=MediaType.MULTIPART_FORM_DATA_VALUE)@ResponseStatus(HttpStatus.CREATED)public CandidateCv upload(@RequestPart("file")MultipartFile file,@AuthenticationPrincipal Jwt jwt){
return service.upload(id(jwt),file);
}
@DeleteMapping("/{cvId}")@ResponseStatus(HttpStatus.NO_CONTENT)public void delete(@PathVariable UUID cvId,@AuthenticationPrincipal Jwt jwt){
service.delete(cvId,id(jwt));
}
}

