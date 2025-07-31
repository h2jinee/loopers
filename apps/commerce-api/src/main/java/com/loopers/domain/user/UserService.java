package com.loopers.domain.user;

import org.springframework.stereotype.Service;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public UserEntity createUser(UserCommand.Create command) {
        // 중복체크
        if (userRepository.existsByUserId(command.userId().value())) {
            throw new CoreException(ErrorType.BAD_REQUEST, "이미 존재하는 ID 입니다.");
        }

        // 엔티티 생성
        UserEntity user = new UserEntity(
            command.userId().value(),
            command.name(),
            command.gender(),
            command.birth().value(),
            command.email().value()
        );

        return userRepository.save(user);
    }
    
    public UserEntity getUserInfo(String userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new CoreException(
                ErrorType.NOT_FOUND, 
                "존재하지 않는 사용자입니다."
            ));
    }
}
