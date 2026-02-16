package org.ecommerce.v1.repository;

import org.ecommerce.v1.entity.Order;
import org.ecommerce.v1.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order,Long> {

    Page<Order> findByUserId(Long userId, Pageable pageable);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    Page<Order> findByUserIdAndStatus(Long userId, OrderStatus status, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.user.id = :userId ORDER BY o.createdAt DESC")
    List<Order> findOrderHistoryByUserId(@Param("userId") Long userId);

    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate")
    List<Order> findOrdersBetweenDates(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status = :status")
    BigDecimal calculateTotalRevenueByStatus(@Param("status") OrderStatus status);

    @Query(value = "SELECT DATE(created_at) as order_date, COUNT(*) as order_count, SUM(total_amount) as daily_revenue " +
            "FROM orders WHERE deleted = false " +
            "GROUP BY DATE(created_at) ORDER BY order_date DESC LIMIT :days",
            nativeQuery = true)
    List<Object[]> getDailySalesReport(@Param("days") int days);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.user.id = :userId")
    Long countOrdersByUserId(@Param("userId") Long userId);
}
