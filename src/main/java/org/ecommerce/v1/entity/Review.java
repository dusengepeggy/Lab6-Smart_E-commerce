package org.ecommerce.v1.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Setter
@Getter
@Table(name = "reviews")
@SQLDelete(sql = "UPDATE reviews SET deleted = true, deleted_at = now() WHERE id = ?")
@SQLRestriction("deleted = false")
@NoArgsConstructor
public class Review extends BaseEntity{
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id",
            nullable = false
    )
    private User user;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id",
            nullable = false
    )
    private Product product;
    private Integer rating;
    private String comment;
}
