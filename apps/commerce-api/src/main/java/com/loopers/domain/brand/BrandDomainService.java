package com.loopers.domain.brand;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BrandDomainService {
    
    private final BrandRepository brandRepository;
    
    public BrandEntity getBrand(BrandCommand.GetOne command) {
        return brandRepository.findById(command.brandId())
            .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "브랜드를 찾을 수 없습니다."));
    }
    
    public Page<BrandEntity> getBrandList(BrandCommand.GetList command) {
        Pageable pageable = PageRequest.of(
            command.page(), 
            command.size(), 
            Sort.by(Sort.Direction.DESC, "createdAt")
        );
        
        return brandRepository.findAll(pageable);
    }
}
