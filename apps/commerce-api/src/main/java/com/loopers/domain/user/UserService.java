package com.loopers.domain.user;

import org.springframework.stereotype.Service;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class UserService {

	private final UserRepository userRepository;

	public UserEntity save(UserEntity userEntity) {
		// 중복체크
		if (userRepository.findByUserId(userEntity.getUserId()).isPresent()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "이미 존재하는 ID 입니다.");
        }
		return userRepository.save(userEntity);
	}
}
