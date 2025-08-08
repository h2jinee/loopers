package com.loopers.domain.user;

import org.springframework.stereotype.Service;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.infrastructure.user.UserJpaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserJpaRepository userJpaRepository;

    public UserEntity createUser(UserCommand.Create command) {
        // 1. 사용자 ID 중복 검증
        if (userJpaRepository.existsByUserId(command.userId().value())) {
            throw new CoreException(ErrorType.BAD_REQUEST, "이미 존재하는 ID 입니다.");
        }

        // 2. 사용자 엔티티 생성
        UserEntity user = new UserEntity(
            command.userId().value(),
            command.name(),
            command.gender(),
            command.birth().value(),
            command.email().value()
        );

        // 3. 사용자 정보 저장
        return userJpaRepository.save(user);
    }
    
    public UserEntity getUserInfo(UserCommand.GetOne command) {
        // 사용자 ID로 조회, 없으면 예외 발생
        return userJpaRepository.findById(command.userId())
            .orElseThrow(() -> new CoreException(
                ErrorType.NOT_FOUND, 
                "존재하지 않는 사용자입니다."
            ));
    }
}
