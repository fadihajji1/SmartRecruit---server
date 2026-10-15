package com.smartrecruit.identity.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

@Entity
@Table(name = "administrator_profiles")
@PrimaryKeyJoinColumn(name = "user_id")
@DiscriminatorValue("PLATFORM_ADMINISTRATOR")
public class PlatformAdministrator extends User {
	@Column(name = "admin_notes", length = 1000)
	private String adminNotes;

	protected PlatformAdministrator() {
	}

	public PlatformAdministrator(
		String email, 
		String passwordHash, 
		String fullName
	) {
		super(
			email, 
			passwordHash,
			fullName,
			UserRole.PLATFORM_ADMINISTRATOR,
			AccountStatus.ACTIVE
		);
	}
}
