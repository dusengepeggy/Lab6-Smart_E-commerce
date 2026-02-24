package org.ecommerce.v1.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;

@Entity
@Setter
@Getter
@Table(name = "products",
    indexes = {
        @Index(name = "idx_product_name", columnList = "product_name"),
            @Index(name = "idx_product_category", columnList = "category_id" )

    })
@SQLDelete(sql = "UPDATE products SET deleted = true, deleted_at = now() WHERE id = ?")
@SQLRestriction("deleted = false")
@NoArgsConstructor
public class Product extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
    @Column(nullable = false, name = "product_name")
    private String productName;
    @Column(columnDefinition = "TEXT")
    private String description;
    @Column(nullable = false)
    private BigDecimal price;

}
