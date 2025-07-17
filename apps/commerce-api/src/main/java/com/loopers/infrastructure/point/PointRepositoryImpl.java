package com.loopers.infrastructure.point;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.loopers.domain.point.PointEntity;
import com.loopers.domain.point.PointRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class PointRepositoryImpl implements PointRepository {

	// TODO DB 추가 시 Map -> DB 변경해야 함.
	private final Map<String, PointEntity> store = new HashMap<>();

	@Override
	public PointEntity save(PointEntity pointEntity) {
		store.put(pointEntity.getUserId(), pointEntity);
		return pointEntity;
	}

	@Override
	public Optional<PointEntity> findByUserId(String userId) {
		return Optional.ofNullable(store.get(userId));
	}

	@Override
	public void clear() {
		store.clear();
	}
}
