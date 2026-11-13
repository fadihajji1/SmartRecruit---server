package com.smartrecruit.identity.service;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.smartrecruit.identity.dto.AdminCreateUserRequest;
import com.smartrecruit.identity.dto.AdminUpdateUserRequest;
import com.smartrecruit.identity.dto.UserResponse;
import com.smartrecruit.identity.exception.StorageException;
import com.smartrecruit.identity.model.AccountStatus;
import com.smartrecruit.identity.model.Candidate;
import com.smartrecruit.identity.model.PlatformAdministrator;
import com.smartrecruit.identity.model.Recruiter;
import com.smartrecruit.identity.model.User;
import com.smartrecruit.identity.model.UserRole;
import com.smartrecruit.identity.repository.UserRepository;
import com.smartrecruit.identity.storage.EmploymentProofStorage;
import com.smartrecruit.identity.storage.EmploymentProofStorage.StoredEmploymentProof;
import org.springframework.security.crypto.password.PasswordEncoder;

@Service
public class AdminUserService {
	private final UserRepository users;
	private final PasswordEncoder passwordEncoder;
	private final EmploymentProofStorage employmentProofStorage;

	public AdminUserService(UserRepository users, PasswordEncoder passwordEncoder,
			EmploymentProofStorage employmentProofStorage) {
		this.users = users;
		this.passwordEncoder = passwordEncoder;
		this.employmentProofStorage = employmentProofStorage;
	}

	@Transactional
	public UserResponse create(AdminCreateUserRequest request, MultipartFile employmentProof) {
		String email = request.email().trim().toLowerCase(Locale.ROOT);
		if (users.existsByEmailIgnoreCase(email)) {
			throw new EmailAlreadyRegisteredException();
		}
		if (request.password().getBytes(StandardCharsets.UTF_8).length > 72) {
			throw new IllegalArgumentException("Password must be at most 72 UTF-8 bytes");
		}

		String passwordHash = passwordEncoder.encode(request.password());
		User user;
		if (request.role() == UserRole.CANDIDATE) {
			if (employmentProof != null && !employmentProof.isEmpty()) {
				throw new IllegalArgumentException("Employment proof is only accepted for recruiter accounts");
			}
			user = new Candidate(email, passwordHash, request.fullName().trim());
		} else if (request.role() == UserRole.RECRUITER) {
			if (employmentProof == null || employmentProof.isEmpty()) {
				throw new IllegalArgumentException("Employment proof is required for recruiter accounts");
			}
			String proofKey = employmentProofStorage.store(employmentProof);
			user = new Recruiter(email, passwordHash, request.fullName().trim(), proofKey);
		} else {
			if (employmentProof != null && !employmentProof.isEmpty()) {
				throw new IllegalArgumentException("Employment proof is only accepted for recruiter accounts");
			}
			user = new PlatformAdministrator(email, passwordHash, request.fullName().trim());
		}
		user.setProfile(request.fullName().trim(), request.phone(), request.address(),
				request.city(), request.state(), request.country(), request.postalCode());
		try {
			return UserResponse.from(users.saveAndFlush(user));
		} catch (DataIntegrityViolationException exception) {
			if (user instanceof Recruiter recruiter) {
				try {
					employmentProofStorage.delete(recruiter.getEmploymentProofObjectKey());
				} catch (StorageException cleanupException) {
					throw cleanupException;
				}
			}
			if (isUniqueConstraintViolation(exception)) {
				throw new EmailAlreadyRegisteredException();
			}
			throw exception;
		}
	}

	@Transactional(readOnly = true)
	public List<UserResponse> getAll() {
		return users.findAll().stream().map(UserResponse::from).toList();
	}

	@Transactional(readOnly = true)
	public UserResponse get(UUID id) {
		return UserResponse.from(findUser(id));
	}

	@Transactional(readOnly = true)
	public List<UserResponse> getPendingRecruiters() {
		return users.findByRoleAndStatus(UserRole.RECRUITER, AccountStatus.PENDING)
				.stream().map(UserResponse::from).toList();
	}

	@Transactional
	public UserResponse approveRecruiter(UUID id) {
		return decideRecruiter(id, AccountStatus.ACTIVE);
	}

	@Transactional
	public UserResponse rejectRecruiter(UUID id) {
		return decideRecruiter(id, AccountStatus.REJECTED);
	}

	@Transactional(readOnly = true)
	public StoredEmploymentProof getEmploymentProof(UUID id) {
		User user = findUser(id);
		if (!(user instanceof Recruiter recruiter)) {
			throw new IllegalArgumentException("Employment proof is only available for recruiter accounts");
		}
		return employmentProofStorage.retrieve(recruiter.getEmploymentProofObjectKey());
	}

	@Transactional
	public UserResponse update(UUID id, AdminUpdateUserRequest request) {
		User user = findUser(id);
		user.setProfile(request.fullName().trim(), request.phone(), request.address(),
				request.city(), request.state(), request.country(), request.postalCode());
		if (user instanceof Recruiter recruiter) {
			recruiter.setApprovalStatus(request.status());
		} else {
			user.setStatus(request.status());
		}
		return UserResponse.from(users.save(user));
	}

	@Transactional
	public void delete(UUID id) {
		User user = findUser(id);
		if (user instanceof Recruiter recruiter) {
			employmentProofStorage.delete(recruiter.getEmploymentProofObjectKey());
		}
		users.delete(user);
	}

	private User findUser(UUID id) {
		return users.findById(id).orElseThrow(UserNotFoundException::new);
	}

	private UserResponse decideRecruiter(UUID id, AccountStatus decision) {
		User user = findUser(id);
		if (!(user instanceof Recruiter recruiter)) {
			throw new IllegalArgumentException("Approval decisions are only available for recruiter accounts");
		}
		if (recruiter.getApprovalStatus() != AccountStatus.PENDING) {
			throw new IllegalArgumentException("Only pending recruiter accounts can be reviewed");
		}
		recruiter.setApprovalStatus(decision);
		return UserResponse.from(users.save(recruiter));
	}

	private static boolean isUniqueConstraintViolation(DataIntegrityViolationException exception) {
		Throwable cause = exception.getMostSpecificCause();
		return cause instanceof SQLException sqlException && "23505".equals(sqlException.getSQLState());
	}
}