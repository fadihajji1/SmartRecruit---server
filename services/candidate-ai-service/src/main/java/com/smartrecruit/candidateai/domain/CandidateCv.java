package com.smartrecruit.candidateai.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name="candidate_cvs") public class CandidateCv {
 @Id public UUID id;
 public UUID candidateId;
 public String originalFilename,objectKey,contentType;
 public long sizeBytes;
 public Instant createdAt,deletedAt;
 @PrePersist void create(){
id=id==null?UUID.randomUUID():id;
createdAt=Instant.now();
}
 }

