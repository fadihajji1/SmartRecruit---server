package com.smartrecruit.recruitment.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name="applications",uniqueConstraints=@UniqueConstraint(columnNames={
"job_id","candidate_id"}
)) public class Application {
 @Id public UUID id;
public UUID jobId,candidateId,candidateCvId;
@Column(columnDefinition="text") public String message;
@Enumerated(EnumType.STRING)public Status status;
public Instant createdAt,updatedAt;
public enum Status{
PENDING,HIRED,REJECTED,UNAVAILABLE}
@PrePersist void create(){
id=id==null?UUID.randomUUID():id;
createdAt=updatedAt=Instant.now();
}
@PreUpdate void update(){
updatedAt=Instant.now();
}
}

