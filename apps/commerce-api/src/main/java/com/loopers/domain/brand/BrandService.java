package com.loopers.domain.brand;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.infrastructure.brand.BrandJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BrandService {

    private final BrandJpaRepository brandJpaRepository;

    public BrandEntity getBrand(BrandCommand.GetOne command) {
        // 브랜드 ID로 조회, 없으면 예외 발생
        return brandJpaRepository.findById(command.brandId())
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "브랜드를 찾을 수 없습니다."));
    }

    public Page<BrandEntity> getBrandList(BrandCommand.GetList command) {
        // 생성일 역순 정렬로 페이지 조회
        Pageable pageable = PageRequest.of(
            command.page(),
            command.size(),
            Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return brandJpaRepository.findAll(pageable);
    }
}
