package com.smartrecruit.identity.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "candidate_profiles")
@PrimaryKeyJoinColumn(name = "user_id")
@DiscriminatorValue("CANDIDATE")
public class Candidate extends User {
	
	protected Candidate() {
	}

	public Candidate(
		String email,
		String passwordHash, 
		String fullName
	) {
		super(
			email, passwordHash, fullName, UserRole.CANDIDATE, AccountStatus.ACTIVE);
	}
}
