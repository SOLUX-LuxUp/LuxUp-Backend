package com.taptap.backend.user.repository;

import com.taptap.backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    boolean existsByEmailAndStatus(String email, String status);

    Optional<User> findByEmail(String email);

    Optional<User> findByGoogleSub(String googleSub);
}