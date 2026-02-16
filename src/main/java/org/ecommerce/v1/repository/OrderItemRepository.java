package org.ecommerce.v1.repository;

import org.ecommerce.v1.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem,Long> {

    List<OrderItem> findByOrderId(Long orderId);

    List<OrderItem> findByProductId(Long productId);

    @Query("SELECT oi.product.id, oi.product.productName, SUM(oi.quantity) as totalSold " +
            "FROM OrderItem oi GROUP BY oi.product.id, oi.product.productName ORDER BY totalSold DESC")
    List<Object[]> findTopSellingProducts();

    @Query(value = "SELECT p.id, p.product_name, SUM(oi.quantity) as total_sold " +
            "FROM order_items oi JOIN products p ON oi.product_id = p.id " +
            "WHERE oi.deleted = false " +
            "GROUP BY p.id, p.product_name ORDER BY total_sold DESC LIMIT :limit",
            nativeQuery = true)
    List<Object[]> findTopSellingProductsLimited(@Param("limit") int limit);

    @Query("SELECT SUM(oi.quantity) FROM OrderItem oi WHERE oi.product.id = :productId")
    Long getTotalQuantitySoldByProduct(@Param("productId") Long productId);
}
