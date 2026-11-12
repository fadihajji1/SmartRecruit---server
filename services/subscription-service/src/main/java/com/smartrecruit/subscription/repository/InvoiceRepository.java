package com.smartrecruit.subscription.repository;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.smartrecruit.subscription.domain.Invoice;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
    Page<Invoice> findAllByOwnerIdOrderByIssuedAtDesc(UUID ownerId, Pageable pageable);
}
