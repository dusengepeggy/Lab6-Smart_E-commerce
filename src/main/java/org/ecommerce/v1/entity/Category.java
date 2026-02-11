package org.ecommerce.v1.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@Setter
@Getter
@Table(name = "categories")
@SQLDelete( sql = "UPDATE categories SET deleted = true, deleted_at = now() WHERE id = ? ")
@SQLRestriction("deleted = false")
public class Category extends BaseEntity {
    @Column(unique = true,nullable = false)
    private String categoryName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    private List<Product> products = new ArrayList<>();
}
