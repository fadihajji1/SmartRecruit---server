package com.smartrecruit.recruitment.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartrecruit.recruitment.model.BusinessMember;

public interface BusinessMemberRepository extends JpaRepository<BusinessMember, UUID> {
	List<BusinessMember> findAllByUserId(UUID userId);

	Optional<BusinessMember> findByBusiness_IdAndUserId(UUID businessId, UUID userId);

	boolean existsByBusiness_IdAndUserId(UUID businessId, UUID userId);
}
