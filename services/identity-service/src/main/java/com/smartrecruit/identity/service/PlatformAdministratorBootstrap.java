package com.smartrecruit.identity.service;

import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.smartrecruit.identity.model.PlatformAdministrator;
import com.smartrecruit.identity.model.User;
import com.smartrecruit.identity.repository.UserRepository;

@Component
public class PlatformAdministratorBootstrap implements ApplicationRunner {
	private static final Logger logger = LoggerFactory.getLogger(PlatformAdministratorBootstrap.class);
	private static final String ADMIN_EMAIL = "admin@admin.com";
	private static final String ADMIN_NAME = "Platform Administrator";
	private static final int BCRYPT_MAX_PASSWORD_BYTES = 72;

	private final UserRepository users;
	private final PasswordEncoder passwordEncoder;
	private final String adminPassword;

	public PlatformAdministratorBootstrap(UserRepository users, PasswordEncoder passwordEncoder,
			@Value("${identity.admin.password:}") String adminPassword) {
		this.users = users;
		this.passwordEncoder = passwordEncoder;
		this.adminPassword = adminPassword;
	}

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		if (adminPassword.isBlank()) {
			logger.warn("Default platform administrator was not created; configure IDENTITY_ADMIN_PASSWORD");
			return;
		}
		if (adminPassword.getBytes(StandardCharsets.UTF_8).length > BCRYPT_MAX_PASSWORD_BYTES) {
			throw new IllegalStateException("IDENTITY_ADMIN_PASSWORD must be at most 72 UTF-8 bytes");
		}

		User existingUser = users.findByEmailIgnoreCase(ADMIN_EMAIL).orElse(null);
		if (existingUser != null) {
			if (!(existingUser instanceof PlatformAdministrator)) {
				throw new IllegalStateException(
						"Cannot create default platform administrator; admin@admin.com is already in use");
			}
			return;
		}

		users.saveAndFlush(new PlatformAdministrator(ADMIN_EMAIL, passwordEncoder.encode(adminPassword), ADMIN_NAME));
		logger.info("Created default platform administrator account: {}", ADMIN_EMAIL);
	}
}