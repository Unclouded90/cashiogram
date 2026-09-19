package com.mauro.cashiogram.category.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mauro.cashiogram.category.entity.CategoryEntity;

public interface CategoryRepository extends JpaRepository<CategoryEntity, UUID>{
    // Find
    List<CategoryEntity> findByNameContainingIgnoreCase(String name);

    // Boolean
    boolean existsByNameIgnoreCase(String name);
}
