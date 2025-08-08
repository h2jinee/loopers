package com.loopers.infrastructure.product;

import com.loopers.domain.product.ProductCountEntity;
import com.loopers.domain.product.ProductCountRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductCountJpaRepository extends JpaRepository<ProductCountEntity, Long>, ProductCountRepository {

}
