package com.smartrecruit.identity.service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartrecruit.identity.dto.AdminUpdateUserRequest;
import com.smartrecruit.identity.dto.UserResponse;
import com.smartrecruit.identity.model.AccountStatus;
import com.smartrecruit.identity.model.Recruiter;
import com.smartrecruit.identity.model.User;
import com.smartrecruit.identity.model.UserRole;
import com.smartrecruit.identity.repository.UserRepository;
import com.smartrecruit.identity.storage.EmploymentProofStorage;

@Service
public class AdminUserService {
	private final UserRepository users;
	private final EmploymentProofStorage storage;

	public AdminUserService(UserRepository users, EmploymentProofStorage storage) {
		this.users = users;
		this.storage = storage;
	}

	@Transactional(readOnly = true)
	public List<UserResponse> list() {
		return users.findAll().stream()
				.sorted(Comparator.comparing(User::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
				.map(UserResponse::from).toList();
	}

	@Transactional(readOnly = true)
	public UserResponse get(UUID id) {
		return UserResponse.from(find(id));
	}

	@Transactional
	public UserResponse update(UUID id, AdminUpdateUserRequest request, UUID actingAdminId) {
		User user = find(id);
		if (request.fullName() != null || request.phone() != null || request.address() != null || request.city() != null
				|| request.state() != null || request.country() != null || request.postalCode() != null) {
			String name = request.fullName() == null ? user.getFullName() : request.fullName().trim();
			if (name.isBlank()) throw new IllegalArgumentException("Full name is required");
			user.setProfile(name, or(request.phone(), user.getPhone()), or(request.address(), user.getAddress()),
					or(request.city(), user.getCity()), or(request.state(), user.getState()),
					or(request.country(), user.getCountry()), or(request.postalCode(), user.getPostalCode()));
		}
		if (request.status() != null && request.status() != user.getStatus()) {
			if (user.getId().equals(actingAdminId)) throw new IllegalStateException("You cannot change the status of your own account");
			if (user.getRole() == UserRole.PLATFORM_ADMINISTRATOR && request.status() != AccountStatus.ACTIVE
					&& activeAdmins() <= 1) throw new IllegalStateException("At least one active administrator is required");
			setStatus(user, request.status());
		}
		return UserResponse.from(users.save(user));
	}

	@Transactional
	public UserResponse approve(UUID id) {
		Recruiter recruiter = recruiter(id);
		recruiter.setApprovalStatus(AccountStatus.ACTIVE);
		return UserResponse.from(users.save(recruiter));
	}

	@Transactional
	public UserResponse reject(UUID id) {
		Recruiter recruiter = recruiter(id);
		recruiter.setApprovalStatus(AccountStatus.REJECTED);
		return UserResponse.from(users.save(recruiter));
	}

	@Transactional(readOnly = true)
	public Map<String, String> employmentProof(UUID id) {
		Recruiter recruiter = recruiter(id);
		return Map.of("userId", id.toString(), "url", storage.temporaryUrl(recruiter.getEmploymentProofObjectKey()));
	}

	private static void setStatus(User user, AccountStatus status) {
		if (user instanceof Recruiter recruiter) recruiter.setApprovalStatus(status);
		else user.setStatus(status);
	}

	private long activeAdmins() {
		return users.findAll().stream()
				.filter(u -> u.getRole() == UserRole.PLATFORM_ADMINISTRATOR && u.getStatus() == AccountStatus.ACTIVE).count();
	}

	private Recruiter recruiter(UUID id) {
		if (find(id) instanceof Recruiter recruiter) return recruiter;
		throw new IllegalArgumentException("This account is not a recruiter");
	}

	private User find(UUID id) {
		return users.findById(id).orElseThrow(UserNotFoundException::new);
	}

	private static String or(String value, String fallback) {
		return value == null ? fallback : value.isBlank() ? null : value.trim();
	}
}
