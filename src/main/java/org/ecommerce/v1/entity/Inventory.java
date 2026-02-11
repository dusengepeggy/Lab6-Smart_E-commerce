package org.ecommerce.v1.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "inventories",
    indexes = @Index(name = "idx_inventory_quantity",columnList = "stock_quantity")
)
@Setter
@Getter
@NoArgsConstructor
@SQLDelete(sql = "UPDATE inventories SET deleted = true, deleted_at = now() WHERE id = ?")
@SQLRestriction("deleted = false")
public class Inventory extends BaseEntity {
    @OneToOne(fetch = FetchType.LAZY , optional = false)
    @JoinColumn(
            name = "product_id",
            nullable = false,
            unique = true
    )
    private Product product;
    @Column(nullable = false)
    private Long stockQuantity;

}
