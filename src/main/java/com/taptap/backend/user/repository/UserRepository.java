package com.taptap.backend.user.repository;

import com.taptap.backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // 이메일로 활성화된 유저 찾기
    Optional<User> findByEmailAndStatus(String email, String status);

    // ID로 활성화된 유저 찾기
    Optional<User> findByUserIdAndStatus(Long userId, String status);
}