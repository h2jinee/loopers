package com.loopers.infrastructure.user;

import java.util.Optional;

import com.loopers.domain.user.UserEntity;
import com.loopers.domain.user.UserRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserJpaRepository extends JpaRepository<UserEntity, Long>, UserRepository {
    Optional<UserEntity> findByUserId(String userId);
}
