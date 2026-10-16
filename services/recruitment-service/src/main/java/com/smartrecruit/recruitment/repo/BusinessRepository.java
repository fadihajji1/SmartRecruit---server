package com.smartrecruit.recruitment.repo;
import com.smartrecruit.recruitment.domain.Business;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface BusinessRepository extends JpaRepository<Business,UUID> { List<Business> findByOwnerUserId(UUID ownerUserId); }
