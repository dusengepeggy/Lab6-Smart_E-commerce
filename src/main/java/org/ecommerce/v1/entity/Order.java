package org.ecommerce.v1.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "orders",
        indexes = @Index(name = "idx_order_user", columnList = "user_id")
)
@SQLDelete(sql = "UPDATE orders SET deleted = true, deleted_at = now() WHERE id = ?")
@SQLRestriction("deleted = false")
public class Order extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn (
            name = "user_id",
            nullable = false
    )
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.Pending;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @OneToMany(mappedBy = "order" , fetch = FetchType.LAZY)
    private List<OrderItem> orderItems = new ArrayList<>();

}
