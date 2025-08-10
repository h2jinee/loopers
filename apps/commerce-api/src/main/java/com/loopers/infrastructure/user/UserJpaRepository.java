package com.loopers.infrastructure.user;

import java.util.Optional;

import com.loopers.domain.user.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {
    
    boolean existsByUserId(String userId);
    
    Optional<UserEntity> findByUserId(String userId);
}
