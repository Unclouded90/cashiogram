package com.mauro.cashiogram.subcategory.entity;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.mauro.cashiogram.category.entity.CategoryEntity;

@Entity 
@Getter 
@Setter 
@NoArgsConstructor 
@Table(
    name="subcategories",
    uniqueConstraints = @UniqueConstraint(
        columnNames = {"category_id", "name"}
    )
)
public class SubcategoryEntity {
    @Id 
    @GeneratedValue (strategy = GenerationType.UUID)
    @Column(unique = true, nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, updatable = true, length = 255)
    private String name;

    @Column(nullable = true, updatable = true, length = 255)
    private String description;

    @ManyToOne 
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryEntity category;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}
