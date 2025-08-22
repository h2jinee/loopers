package com.loopers.domain.brand;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BrandService {

    private final BrandRepository brandRepository;

    /**
     * 단일 브랜드 조회
     */
    public BrandInfo getBrand(BrandCommand.GetOne command) {
        Brand brand = brandRepository.findById(command.brandId())
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "브랜드를 찾을 수 없습니다."));
        return BrandInfo.from(brand);
    }

    /**
     * 여러 브랜드 벌크 조회
     */
    public Map<Long, BrandInfo> getBrandsByIds(List<Long> brandIds) {
        List<Long> uniqueBrandIds = brandIds.stream().distinct().toList();
        
        List<Brand> brands = brandRepository.findByIdIn(uniqueBrandIds);
        
        return brands.stream()
            .collect(Collectors.toMap(
                Brand::getId,
                BrandInfo::from
            ));
    }

    /**
     * 브랜드 목록 조회
     */
    public Page<BrandInfo> getBrandList(BrandCommand.GetList command) {
        Pageable pageable = PageRequest.of(
            command.page(),
            command.size(),
            Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return brandRepository.findAll(pageable)
            .map(BrandInfo::from);
    }
}
