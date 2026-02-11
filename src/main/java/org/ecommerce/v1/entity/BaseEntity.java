package org.ecommerce.v1.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@Setter
public abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE , generator = "id_seq")
    @SequenceGenerator(
            name = "id_seq",
            sequenceName = "id_sequence",
            allocationSize = 50
    )
    private Long id;

    @Version
    Long version;

    @Column(updatable = false,nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;
    private boolean deleted = false;
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @PrePersist
    protected void onCreate(){
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.deleted = false;
    }

    @PreUpdate
    protected void onUpdate(){
        this.updatedAt = LocalDateTime.now();
    }

    public void markAsDeleted(){
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
    }


}
