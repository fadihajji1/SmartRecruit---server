package com.smartrecruit.identity.service;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.smartrecruit.identity.dto.CandidateRegistrationRequest;
import com.smartrecruit.identity.dto.LoginRequest;
import com.smartrecruit.identity.dto.RecruiterRegistrationRequest;
import com.smartrecruit.identity.dto.TokenResponse;
import com.smartrecruit.identity.dto.UserResponse;
import com.smartrecruit.identity.exception.StorageException;
import com.smartrecruit.identity.model.Candidate;
import com.smartrecruit.identity.model.Recruiter;
import com.smartrecruit.identity.model.User;
import com.smartrecruit.identity.repository.UserRepository;
import com.smartrecruit.identity.storage.EmploymentProofStorage;

@Service
public class IdentityService {
	private static final Logger logger = LoggerFactory.getLogger(IdentityService.class);

	private final UserRepository users;
	private final PasswordEncoder passwordEncoder;
	private final EmploymentProofStorage employmentProofStorage;
	private final AuthenticationManager authenticationManager;
	private final TokenService tokenService;

	public IdentityService(UserRepository users, PasswordEncoder passwordEncoder,
			EmploymentProofStorage employmentProofStorage, AuthenticationManager authenticationManager,
			TokenService tokenService) {
		this.users = users;
		this.passwordEncoder = passwordEncoder;
		this.employmentProofStorage = employmentProofStorage;
		this.authenticationManager = authenticationManager;
		this.tokenService = tokenService;
	}

	@Transactional
	public UserResponse registerCandidate(CandidateRegistrationRequest request) {
		String email = normalizeEmail(request.email());
		ensureEmailAvailable(email);
		validatePassword(request.password());

		Candidate candidate = new Candidate(
			email, 
			passwordEncoder.encode(request.password()), request.fullName().trim()
			);
		
			applyProfile(
				candidate, 
				request.phone(), 
				request.address(), 
				request.city(), 
				request.state(), 
				request.country(), 
				request.postalCode())
				;
		return UserResponse.from(save(candidate));
	}

	@Transactional
	public UserResponse registerRecruiter(RecruiterRegistrationRequest request, MultipartFile employmentProof) {
		String email = normalizeEmail(request.email());
		ensureEmailAvailable(email);
		validatePassword(request.password());

		String objectKey = employmentProofStorage.store(employmentProof);

		Recruiter recruiter = new Recruiter(
				email, passwordEncoder.encode(request.password()),
				request.fullName().trim(), objectKey);
		
				applyProfile(
					recruiter,
					request.phone(), 
					request.address(), 
					request.city(), 
					request.state(),
					request.country(), 
					request.postalCode()
				);
		try {
			return UserResponse.from(users.saveAndFlush(recruiter));
		} catch (DataIntegrityViolationException exception) {
			try {
				employmentProofStorage.delete(objectKey);
			} catch (StorageException cleanupException) {
				logger.error("Could not remove employment proof after recruiter registration failed: {}", objectKey,
						cleanupException);
			}
			if (isUniqueConstraintViolation(exception)) {
				throw new EmailAlreadyRegisteredException();
			}
			throw exception;
		}
	}

	public TokenResponse login(LoginRequest request) {
		String email = normalizeEmail(request.email());
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(email, request.password()));
		User user = (User) authentication.getPrincipal();
		return tokenService.issue(user);
	}

	private User save(User user) {
		try {
			return users.saveAndFlush(user);
		} catch (DataIntegrityViolationException exception) {
			if (isUniqueConstraintViolation(exception)) {
				throw new EmailAlreadyRegisteredException();
			}
			throw exception;
		}
	}

	private static boolean isUniqueConstraintViolation(DataIntegrityViolationException exception) {
		Throwable cause = exception.getMostSpecificCause();
		return cause instanceof SQLException sqlException && "23505".equals(sqlException.getSQLState());
	}

	private void ensureEmailAvailable(String email) {
		if (users.existsByEmailIgnoreCase(email)) {
			throw new EmailAlreadyRegisteredException();
		}
	}

	private static String normalizeEmail(String email) {
		return email.trim().toLowerCase(Locale.ROOT);
	}

	private static void validatePassword(String password) {
		if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
			throw new IllegalArgumentException("Password must be at most 72 UTF-8 bytes");
		}
	}

	private static void applyProfile(User user, String phone, String address, String city, String state,
			String country, String postalCode) {
		user.setProfile(user.getFullName(), phone, address, city, state, country, postalCode);
	}
}
