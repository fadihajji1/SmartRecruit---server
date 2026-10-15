package com.smartrecruit.interview.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name="interviews") public class Interview {
 @Id public UUID id;
 public UUID applicationId,candidateId,recruiterId,jobId;
 public Instant scheduledAt;
 public String externalMeetingUrl;
 public String status;
 @PrePersist void create(){
id=id==null?UUID.randomUUID():id;
}
 }

