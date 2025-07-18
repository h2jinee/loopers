package com.loopers.domain.user;

import org.springframework.stereotype.Service;

import com.loopers.domain.point.PointEntity;
import com.loopers.domain.point.PointRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
	private final UserRepository userRepository;
	private final PointRepository pointRepository;

	public UserEntity save(UserEntity userEntity) {
		// 중복체크
		if (userRepository.findByUserId(userEntity.getUserId()).isPresent()) {
            throw new CoreException(ErrorType.BAD_REQUEST, "이미 존재하는 ID 입니다.");
        }
		// 사용자 저장
		UserEntity savedUser = userRepository.save(userEntity);

		// 포인트 초기 설정
		pointRepository.save(new PointEntity(savedUser.getUserId(), 0L));
		return savedUser;
	}

	public UserEntity getUserInfo(String userId) {
		if (userId == null) {
			throw new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 사용자입니다.");
		}
		return userRepository.findByUserId(userId).orElse(null);
	}
}
