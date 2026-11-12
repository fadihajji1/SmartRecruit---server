package com.smartrecruit.candidateai.api;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import org.springframework.http.CacheControl;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import com.smartrecruit.candidateai.dto.CandidateCvResponse;
import com.smartrecruit.candidateai.dto.CvAnalysisRequest;
import com.smartrecruit.candidateai.dto.CvAnalysisResponse;
import com.smartrecruit.candidateai.dto.RecommendationResponse;
import com.smartrecruit.candidateai.service.CandidateAiService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/candidate")
@Validated
@PreAuthorize("hasRole('CANDIDATE')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Candidate CV and AI", description = "Private CV management, ATS analysis and job recommendations")
public class CvController {
    private final CandidateAiService service;
    public CvController(CandidateAiService service) { this.service = service; }

    @GetMapping("/cvs") @Operation(summary = "List my CVs")
    public List<CandidateCvResponse> list(@AuthenticationPrincipal Jwt jwt) { return service.list(actorId(jwt)); }

    @PostMapping(value = "/cvs", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED) @Operation(summary = "Upload a private CV")
    public CandidateCvResponse upload(@AuthenticationPrincipal Jwt jwt, @RequestParam("file") MultipartFile file) {
        return service.upload(actorId(jwt), file);
    }

    @GetMapping("/cvs/{cvId}") @Operation(summary = "Get one of my CV records")
    public CandidateCvResponse get(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID cvId) { return service.get(actorId(jwt), cvId); }

    @GetMapping("/cvs/{cvId}/download") @Operation(summary = "Download a private CV")
    public ResponseEntity<byte[]> download(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID cvId) {
        CandidateAiService.DownloadedCv cv = service.download(actorId(jwt), cvId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(cv.contentType()));
        headers.setContentDisposition(ContentDisposition.attachment().filename(cv.filename(), StandardCharsets.UTF_8).build());
        headers.setCacheControl(CacheControl.noStore());
        headers.setContentLength(cv.contents().length);
        return ResponseEntity.ok().headers(headers).body(cv.contents());
    }

    @DeleteMapping("/cvs/{cvId}") @ResponseStatus(HttpStatus.NO_CONTENT) @Operation(summary = "Delete a CV and its private object")
    public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID cvId) { service.delete(actorId(jwt), cvId); }

    @PostMapping("/cvs/{cvId}/ats-scan") @Operation(summary = "Run an ATS analysis")
    public CvAnalysisResponse scan(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID cvId,
            @Valid @RequestBody(required = false) CvAnalysisRequest request) { return service.scan(actorId(jwt), cvId, request); }

    @GetMapping("/recommendations") @Operation(summary = "Recommend published jobs from my latest CV analysis")
    public List<RecommendationResponse> recommendations(@AuthenticationPrincipal Jwt jwt) { return service.recommendations(actorId(jwt)); }

    private UUID actorId(Jwt jwt) {
        String value = jwt.getClaimAsString("user_id");
        if (value == null || value.isBlank()) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Token has no user id");
        return UUID.fromString(value);
    }
}
