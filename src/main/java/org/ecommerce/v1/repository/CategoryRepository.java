package org.ecommerce.v1.repository;

import org.ecommerce.v1.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


public interface CategoryRepository extends JpaRepository<Category,Long> {
    Page<Category> findByCategoryNameContainingIgnoreCase(String categoryName, Pageable pageable);
    boolean existsByCategoryName(String categoryName);

}
