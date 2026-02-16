package org.ecommerce.v1.repository;

import org.ecommerce.v1.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<Review,Long> {

    Page<Review> findByProductId(Long productId, Pageable pageable);

    Page<Review> findByUserId(Long userId, Pageable pageable);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId")
    Double getAverageRatingByProductId(@Param("productId") Long productId);

    @Query("SELECT COUNT(r) FROM Review r WHERE r.product.id = :productId")
    Long countReviewsByProductId(@Param("productId") Long productId);

    @Query("SELECT r FROM Review r WHERE r.rating >= :minRating ORDER BY r.createdAt DESC")
    Page<Review> findHighRatedReviews(@Param("minRating") Integer minRating, Pageable pageable);
}
