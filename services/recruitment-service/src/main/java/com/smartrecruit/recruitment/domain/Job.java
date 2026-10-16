package com.smartrecruit.recruitment.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name="jobs") public class Job {
 @Id public UUID id;
public UUID businessId,createdByUserId;
@NotBlank @Size(max=200) public String title;
@Size(max=150) public String location;
@Size(max=80) public String contractType;
@Column(columnDefinition="text")public String description;
@Enumerated(EnumType.STRING)public Status status;
public Instant createdAt,updatedAt,publishedAt,expiresAt;
public enum Status{
DRAFT,PUBLISHED,CLOSED,EXPIRED,DELETED}
@PrePersist void create(){
id=id==null?UUID.randomUUID():id;
createdAt=updatedAt=Instant.now();
}
@PreUpdate void update(){
updatedAt=Instant.now();
}
}

