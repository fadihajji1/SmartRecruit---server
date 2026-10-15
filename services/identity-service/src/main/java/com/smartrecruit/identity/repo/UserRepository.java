package com.smartrecruit.identity.repo;
 import com.smartrecruit.identity.domain.UserAccount;
 import java.util.*;
 import org.springframework.data.jpa.repository.JpaRepository;
 public interface UserRepository extends JpaRepository<UserAccount,UUID>{
Optional<UserAccount> findByEmailIgnoreCase(String email);
boolean existsByEmailIgnoreCase(String email);
}

