package com.smartrecruit.recruitment.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartrecruit.recruitment.model.OutboxMessage;

public interface OutboxRepository extends JpaRepository<OutboxMessage, UUID> {
	List<OutboxMessage> findTop100ByPublishedAtIsNullOrderByCreatedAtAsc();
}
