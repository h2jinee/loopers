package com.loopers.domain.user;

import org.springframework.stereotype.Service;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User createUser(UserCommand.Create command) {
        // 1. 사용자 ID 중복 검증
        if (userRepository.existsByUserId(command.userId().value())) {
            throw new CoreException(ErrorType.BAD_REQUEST, "이미 존재하는 ID 입니다.");
        }

        // 2. 사용자 엔티티 생성 및 저장
        return userRepository.save(User.create(command));
    }
    
    public User getUserInfo(UserCommand.GetOne command) {
        // 사용자 ID로 조회, 없으면 예외 발생
        return userRepository.findByUserId(command.userId())
            .orElseThrow(() -> new CoreException(
                ErrorType.NOT_FOUND, 
                "존재하지 않는 사용자입니다."
            ));
    }
}
