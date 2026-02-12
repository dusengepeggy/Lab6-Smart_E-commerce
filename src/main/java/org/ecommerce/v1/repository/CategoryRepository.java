package org.ecommerce.v1.repository;

import org.ecommerce.v1.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category,Long> {
    Optional<Category> findByCategoryName (String categoryName);
    boolean existsByCategoryName(String categoryName);

}
