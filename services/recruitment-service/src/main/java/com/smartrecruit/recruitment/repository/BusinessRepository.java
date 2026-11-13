package com.smartrecruit.recruitment.repository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartrecruit.recruitment.model.Business;

public interface BusinessRepository extends JpaRepository<Business, UUID> {
	List<Business> findAllByIdIn(Collection<UUID> ids);
}
