package com.smartrecruit.analytics.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name="analytics_metrics") public class Metric {
 @Id public UUID id;
 public UUID recruiterId;
 public String metricKey;
 public long metricValue;
 public Instant measuredAt;
 @PrePersist void create(){
id=id==null?UUID.randomUUID():id;
}
 }

