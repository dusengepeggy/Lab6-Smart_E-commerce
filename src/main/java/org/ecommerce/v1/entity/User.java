package org.ecommerce.v1.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "users")
@Getter
@Setter
@SQLDelete(sql = "UPDATE users SET deleted = true, deleted_at = now() WHERE id = ?")
@SQLRestriction("deleted = false")
@NoArgsConstructor
public class User extends BaseEntity {
    @Column(nullable = false, unique = true)
    private String username;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;
}
