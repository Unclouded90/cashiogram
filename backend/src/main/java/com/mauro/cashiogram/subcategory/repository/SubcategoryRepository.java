package com.mauro.cashiogram.subcategory.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mauro.cashiogram.subcategory.entity.SubcategoryEntity;

public interface SubcategoryRepository extends JpaRepository<SubcategoryEntity, UUID>{
    // Find
    List<SubcategoryEntity> findByNameContainingIgnoreCase(String name);
    List<SubcategoryEntity> findByCategoryId(UUID categoryId);
    List<SubcategoryEntity> findByCategoryIdAndNameContainingIgnoreCase(
        UUID categoryId,
        String name
    );
    
    // Boolean
    boolean existsByNameIgnoreCase(String name);
    boolean existsByCategoryIdAndNameIgnoreCase(
        UUID categoryId,
        String name
    );
}
