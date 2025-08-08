package com.loopers.domain.user;

public interface UserRepository {
	boolean existsByUserId(String id);
}
