package org.ecommerce.v1.repository;

import org.ecommerce.v1.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product,Long> {

    @EntityGraph(attributePaths = {"category"})
    Page<Product> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"category"})
    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

    @EntityGraph(attributePaths = {"category"})
    Page<Product> findByProductNameContainingIgnoreCase(String name, Pageable pageable);

    @EntityGraph(attributePaths = {"category"})
    Page<Product> findByPriceBetween(BigDecimal min, BigDecimal max, Pageable pageable);

    @Query("SELECT p FROM Product p JOIN p.category c WHERE c.categoryName = :categoryName")
    Page<Product> findByCategoryName(@Param("categoryName") String categoryName, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.price <= :maxPrice ORDER BY p.price ASC")
    List<Product> findAffordableProducts(@Param("maxPrice") BigDecimal maxPrice);

    @Query(value = "SELECT p.* FROM products p " +
            "JOIN inventories i ON p.id = i.product_id " +
            "WHERE i.stock_quantity > 0 AND p.deleted = false " +
            "ORDER BY i.stock_quantity DESC",
            nativeQuery = true)
    List<Product> findInStockProductsOrderedByAvailability();

    @Query("SELECT p FROM Product p WHERE p.price >= :minPrice AND p.price <= :maxPrice AND p.category.id = :categoryId")
    Page<Product> findByCategoryAndPriceRange(
            @Param("categoryId") Long categoryId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);

    @Query(value = "SELECT COUNT(*) FROM products WHERE category_id = :categoryId AND deleted = false", nativeQuery = true)
    Long countProductsByCategory(@Param("categoryId") Long categoryId);
}
