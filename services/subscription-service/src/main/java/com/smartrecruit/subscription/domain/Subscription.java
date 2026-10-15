package com.smartrecruit.subscription.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name="subscriptions") public class Subscription {
 @Id public UUID id;
 public UUID ownerId;
 public String planCode;
 public String status;
 public Instant startsAt;
 public Instant endsAt;
 @PrePersist void create(){
id=id==null?UUID.randomUUID():id;
}
 }

