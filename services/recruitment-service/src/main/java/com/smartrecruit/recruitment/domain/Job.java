package com.smartrecruit.recruitment.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name="jobs") public class Job {
 @Id public UUID id;
public UUID businessId,createdByUserId;
public String title,location,contractType;
@Column(columnDefinition="text")public String description;
@Enumerated(EnumType.STRING)public Status status;
public Instant createdAt,updatedAt;
public enum Status{
DRAFT,PUBLISHED,CLOSED,DELETED}
@PrePersist void create(){
id=id==null?UUID.randomUUID():id;
createdAt=updatedAt=Instant.now();
}
@PreUpdate void update(){
updatedAt=Instant.now();
}
}

