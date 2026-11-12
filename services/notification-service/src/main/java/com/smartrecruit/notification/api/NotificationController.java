package com.smartrecruit.notification.api;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import com.smartrecruit.notification.dto.NotificationResponse;
import com.smartrecruit.notification.service.NotificationService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1/notifications")
@Validated
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Notifications", description = "Read and manage the authenticated user's notification inbox")
public class NotificationController {
    private final NotificationService service;
    public NotificationController(NotificationService service) { this.service = service; }

    @GetMapping @Operation(summary = "List my notifications")
    public Page<NotificationResponse> list(@AuthenticationPrincipal Jwt jwt, @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return service.list(actorId(jwt), PageRequest.of(page, size));
    }

    @PatchMapping("/{notificationId}/read") @Operation(summary = "Mark a notification read")
    public NotificationResponse read(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID notificationId) { return service.read(actorId(jwt), notificationId); }

    @PatchMapping("/{notificationId}/unread") @Operation(summary = "Mark a notification unread")
    public NotificationResponse unread(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID notificationId) { return service.unread(actorId(jwt), notificationId); }

    @DeleteMapping("/{notificationId}") @ResponseStatus(HttpStatus.NO_CONTENT) @Operation(summary = "Delete a notification from my inbox")
    public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID notificationId) { service.delete(actorId(jwt), notificationId); }

    private UUID actorId(Jwt jwt) {
        String value = jwt.getClaimAsString("user_id");
        if (value == null || value.isBlank()) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Token has no user id");
        return UUID.fromString(value);
    }
}
