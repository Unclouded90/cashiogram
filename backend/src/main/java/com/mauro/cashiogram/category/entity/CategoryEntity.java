package com.mauro.cashiogram.category.entity;

import java.time.Instant;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter 
@Setter 
@NoArgsConstructor 
@Table (name = "categories")
public class CategoryEntity {
    @Id 
    @GeneratedValue (strategy = GenerationType.UUID)
    @Column (unique = true, updatable = false, nullable = false)
    private UUID id;

    @Column (unique = true, updatable = true, nullable = false, length = 255)
    private String name;

    @Column (updatable = true, nullable = false, length = 255)
    private String description;

    @CreationTimestamp 
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}
