package com.smartrecruit.notification.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.smartrecruit.notification.model.Notification;

/** A user's inbox = notifications addressed to their user id (events) or to their email (manual). */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

	@Query("""
			select n from Notification n
			where (n.recipientUserId = :userId or lower(n.recipientEmail) = lower(:email))
			  and (:read is null or n.read = :read)
			order by n.createdAt desc
			""")
	List<Notification> findInbox(@Param("userId") UUID userId, @Param("email") String email,
			@Param("read") Boolean read);

	@Query("""
			select count(n) from Notification n
			where (n.recipientUserId = :userId or lower(n.recipientEmail) = lower(:email)) and n.read = false
			""")
	long countUnread(@Param("userId") UUID userId, @Param("email") String email);

	@Query("""
			select n from Notification n
			where n.id = :id and (n.recipientUserId = :userId or lower(n.recipientEmail) = lower(:email))
			""")
	Optional<Notification> findOwned(@Param("id") UUID id, @Param("userId") UUID userId, @Param("email") String email);

	boolean existsBySourceEventIdAndRecipientUserId(UUID sourceEventId, UUID recipientUserId);
}
