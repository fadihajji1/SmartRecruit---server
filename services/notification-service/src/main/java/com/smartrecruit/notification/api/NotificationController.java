package com.smartrecruit.notification.api;

import com.smartrecruit.notification.domain.Notification;
import com.smartrecruit.notification.repo.NotificationRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
@RestController@RequestMapping("/api/v1/notifications")public class NotificationController{
private final NotificationRepository repo;
public NotificationController(NotificationRepository r){
repo=r;
}
private UUID uid(Jwt j){
return UUID.fromString(j.getClaimAsString("user_id"));
}
@GetMapping public Page<Notification> all(@AuthenticationPrincipal Jwt j,@RequestParam(defaultValue="0")int page){
return repo.findByRecipientUserIdAndDeletedAtIsNull(uid(j),PageRequest.of(page,20,Sort.by("createdAt").descending()));
}
@PatchMapping("/{id}/read")@Transactional public Notification read(@PathVariable UUID id,@AuthenticationPrincipal Jwt j){
var n=own(id,j);
n.readAt=Instant.now();
return n;
}
@PatchMapping("/{id}/unread")@Transactional public Notification unread(@PathVariable UUID id,@AuthenticationPrincipal Jwt j){
var n=own(id,j);
n.readAt=null;
return n;
}
@DeleteMapping("/{id}")@Transactional public void delete(@PathVariable UUID id,@AuthenticationPrincipal Jwt j){
own(id,j).deletedAt=Instant.now();
}
private Notification own(UUID id,Jwt j){
return repo.findByIdAndRecipientUserIdAndDeletedAtIsNull(id,uid(j)).orElseThrow();
}
}

