package com.smartrecruit.notification.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
@Entity @Table(name="notifications")public class Notification{
@Id public UUID id;
public UUID recipientUserId,sourceEventId;
public String type,title;
@Column(columnDefinition="text")public String content;
public Instant readAt,createdAt,deletedAt;
@PrePersist void create(){
id=id==null?UUID.randomUUID():id;
createdAt=Instant.now();
}
}

