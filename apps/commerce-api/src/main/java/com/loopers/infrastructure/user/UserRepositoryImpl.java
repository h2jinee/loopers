package com.loopers.infrastructure.user;

import com.loopers.domain.user.UserEntity;
import com.loopers.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {
    
    private final UserJpaRepository userJpaRepository;
    
    @Override
    public boolean existsByUserId(String userId) {
        return userJpaRepository.existsByUserId(userId);
    }
    
    @Override
    public UserEntity save(UserEntity user) {
        return userJpaRepository.save(user);
    }
    
    @Override
    public Optional<UserEntity> findByUserId(String userId) {
        return userJpaRepository.findByUserId(userId);
    }
}
