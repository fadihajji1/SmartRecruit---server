package com.smartrecruit.analytics.api;

import java.util.List;
import java.util.UUID;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import com.smartrecruit.analytics.dto.AnalyticsOverviewResponse;
import com.smartrecruit.analytics.dto.ApplicationMetricResponse;
import com.smartrecruit.analytics.dto.OfferMetricResponse;
import com.smartrecruit.analytics.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/recruiter/analytics")
@PreAuthorize("hasRole('RECRUITER')")
@SecurityRequirement(name="bearerAuth")
@Tag(name="Recruiter Analytics",description="Event-driven metrics and report export for the authenticated recruiter")
public class AnalyticsController {
    private final AnalyticsService service;
    public AnalyticsController(AnalyticsService service){this.service=service;}
    @GetMapping("/overview") @Operation(summary="Get recruitment overview")
    public AnalyticsOverviewResponse overview(@AuthenticationPrincipal Jwt jwt){return service.overview(actorId(jwt));}
    @GetMapping("/offers") @Operation(summary="Get offer metrics")
    public List<OfferMetricResponse> offers(@AuthenticationPrincipal Jwt jwt){return service.offers(actorId(jwt));}
    @GetMapping("/applications") @Operation(summary="Get application status metrics")
    public List<ApplicationMetricResponse> applications(@AuthenticationPrincipal Jwt jwt){return service.applications(actorId(jwt));}
    @GetMapping(value="/export/pdf",produces="application/pdf") @Operation(summary="Export the recruitment report as PDF")
    public ResponseEntity<byte[]> export(@AuthenticationPrincipal Jwt jwt){
        byte[] content=service.exportPdf(actorId(jwt));
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
            .header(HttpHeaders.CONTENT_DISPOSITION,ContentDisposition.attachment().filename("smartrecruit-analytics.pdf").build().toString())
            .contentLength(content.length).body(content);
    }
    private UUID actorId(Jwt jwt){String value=jwt.getClaimAsString("user_id");if(value==null||value.isBlank())throw new ResponseStatusException(HttpStatus.FORBIDDEN,"Token has no user id");return UUID.fromString(value);}
}
