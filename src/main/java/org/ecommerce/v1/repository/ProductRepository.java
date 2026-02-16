package org.ecommerce.v1.repository;

import org.ecommerce.v1.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;

public interface ProductRepository extends JpaRepository<Product,Long> {

    @EntityGraph(attributePaths = {"category"})
    Page<Product> findAll(Pageable pageable);
    @EntityGraph(attributePaths = {"category"})
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);
    @EntityGraph(attributePaths = {"category"})
    Page<Product> findByProductNameContainingIgnoreCase(String name, Pageable pageable);
    @EntityGraph(attributePaths = {"category"})
    Page<Product> findByPriceBetween(BigDecimal min, BigDecimal max, Pageable pageable);
}
