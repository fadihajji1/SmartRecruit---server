package com.smartrecruit.identity.model;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(name = "uk_users_email", columnNames = "email"))
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "user_type", discriminatorType = DiscriminatorType.STRING, length = 32)
public abstract class User implements UserDetails {
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private UUID id;

	@Column(nullable = false, length = 320)
	private String email;

	@Column(name = "password_hash", nullable = false)
	private String passwordHash;

	@Column(name = "full_name", nullable = false, length = 100)
	private String fullName;

	@Column(length = 32)
	private String phone;

	@Column(length = 255)
	private String address;

	@Column(length = 100)
	private String city;

	@Column(length = 100)
	private String state;

	@Column(length = 100)
	private String country;

	@Column(name = "postal_code", length = 32)
	private String postalCode;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private UserRole role;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 24)
	private AccountStatus status;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected User() {
	}

	protected User(String email, String passwordHash, String fullName, UserRole role, AccountStatus status) {
		this.email = email;
		this.passwordHash = passwordHash;
		this.fullName = fullName;
		this.role = role;
		this.status = status;
	}
	//set the createdAt and updatedAt fields before persisting or updating the entity in the database
	@PrePersist
	void onCreate() {
		Instant now = Instant.now();
		createdAt = now;
		updatedAt = now;
	}

	@PreUpdate
	void onUpdate() {
		updatedAt = Instant.now();
	}
	
	public UUID getId() {
		return id;
	}

	public String getEmail() {
		return email;
	}

	public String getFullName() {
		return fullName;
	}


	public String getPhone() {
		return phone;
	}

	public String getAddress() {
		return address;
	}

	public String getCity() {
		return city;
	}

	public String getState() {
		return state;
	}

	public String getCountry() {
		return country;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public UserRole getRole() {
		return role;
	}

	public AccountStatus getStatus() {
		return status;
	}

	public void setStatus(AccountStatus status) {
		this.status = status;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void setProfile(String fullName, String phone, String address, String city,String state, String country, String postalCode) {
		this.fullName = fullName;
		this.phone = phone;
		this.address = address;
		this.city = city;
		this.state = state;
		this.country = country;
		this.postalCode = postalCode;
	}

	@Override
	public String getUsername() {
		return email;
	}

	@Override
	public String getPassword() {
		return passwordHash;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
	}

	@Override
	public boolean isEnabled() {
		return status == AccountStatus.ACTIVE;
	}
}
