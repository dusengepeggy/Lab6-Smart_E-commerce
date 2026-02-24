package org.ecommerce.v1.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name="order_items")
@SQLDelete(sql = "UPDATE order_items SET deleted = true, deleted_at = now() WHERE id = ?")
@SQLRestriction("deleted = false")
public class OrderItem extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id",
            nullable = false
    )
    private Order order;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id",
            nullable = false
    )
    private Product product;

    @Column(nullable = false)
    private Long quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;


}
