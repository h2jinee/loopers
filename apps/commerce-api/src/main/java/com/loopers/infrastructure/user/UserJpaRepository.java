package com.loopers.infrastructure.user;

import java.util.Optional;

import com.loopers.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserJpaRepository extends JpaRepository<User, Long> {
    
    boolean existsByUserId(String userId);
    
    Optional<User> findByUserId(String userId);
}
