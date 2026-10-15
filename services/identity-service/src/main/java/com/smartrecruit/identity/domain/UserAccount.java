package com.smartrecruit.identity.domain;

import jakarta.persistence.*;
 import java.time.Instant;
 import java.util.UUID;

@Entity @Table(name="users") public class UserAccount {
 @Id public UUID id;
 @Column(unique=true,nullable=false) public String email;
 @Column(nullable=false) public String passwordHash;
 @Column(nullable=false) public String fullName;
 public String phone,address,city,country;
 @Enumerated(EnumType.STRING) public Role role;
 @Enumerated(EnumType.STRING) public Status status;
 public Instant createdAt,updatedAt;
 @PrePersist void create(){
id=id==null?UUID.randomUUID():id;
createdAt=updatedAt=Instant.now();
}
 @PreUpdate void update(){
updatedAt=Instant.now();
}
 public enum Role{
CANDIDATE,RECRUITER,PLATFORM_ADMINISTRATOR}
 public enum Status{
ACTIVE,PENDING,REJECTED,INACTIVE}
 }

