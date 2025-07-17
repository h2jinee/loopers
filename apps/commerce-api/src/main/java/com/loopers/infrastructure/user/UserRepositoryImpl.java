package com.loopers.infrastructure.user;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.loopers.domain.user.UserEntity;
import com.loopers.domain.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

	// TODO DB 추가 시 Map -> DB 변경해야 함.
	private final Map<String, UserEntity> store = new HashMap<>();

	@Override
	public UserEntity save(UserEntity user) {
		store.put(user.getUserId(), user);
        return user;
	}

	@Override
	public Optional<UserEntity> findByUserId(String userId) {
		return Optional.ofNullable(store.get(userId));
	}

	@Override
	public void clear() {
		store.clear();
	}
}
