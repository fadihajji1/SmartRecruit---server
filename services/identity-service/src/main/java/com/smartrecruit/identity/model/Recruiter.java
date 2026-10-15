package com.smartrecruit.identity.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "recruiter_profiles")
@PrimaryKeyJoinColumn(name = "user_id")
@DiscriminatorValue("RECRUITER")
public class Recruiter extends User {
	@Column(name = "employment_proof_object_key", nullable = false, length = 512)
	private String employmentProofObjectKey;

	@Enumerated(EnumType.STRING)
	@Column(name = "approval_status", nullable = false, length = 24)
	private AccountStatus approvalStatus;

	protected Recruiter() {
	}

	public Recruiter(String email, String passwordHash, String fullName, String employmentProofObjectKey) {
		super(
			email, 
			passwordHash, 
			fullName, 
			UserRole.RECRUITER, 
			AccountStatus.PENDING
		);
		this.employmentProofObjectKey = employmentProofObjectKey;
		this.approvalStatus = AccountStatus.PENDING;
	}

	public String getEmploymentProofObjectKey() {
		return employmentProofObjectKey;
	}

	public AccountStatus getApprovalStatus() {
		return approvalStatus;
	}
}
