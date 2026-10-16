package com.smartrecruit.recruitment.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name="businesses")
public class Business {
    @Id public UUID id;
    @Column(nullable=false) public UUID ownerUserId;
    @Column(nullable=false) public String name;
    public String description, website, industry, address, city, country;
    @Enumerated(EnumType.STRING) @Column(nullable=false) public Status status;
    public Instant createdAt, updatedAt;
    public enum Status { ACTIVE, INACTIVE }
    @PrePersist void create() { id=id==null?UUID.randomUUID():id; status=status==null?Status.ACTIVE:status;createdAt=updatedAt=Instant.now(); }
    @PreUpdate void update() { updatedAt=Instant.now(); }
}
