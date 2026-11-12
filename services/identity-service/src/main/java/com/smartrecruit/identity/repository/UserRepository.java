package com.smartrecruit.identity.repository;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartrecruit.identity.model.User;
import com.smartrecruit.identity.model.AccountStatus;
import com.smartrecruit.identity.model.UserRole;

public interface UserRepository extends JpaRepository<User, UUID> {
	Optional<User> findByEmailIgnoreCase(String email);

	boolean existsByEmailIgnoreCase(String email);

	List<User> findByRoleAndStatus(UserRole role, AccountStatus status);
}
