package com.mauro.cashiogram.category;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mauro.cashiogram.category.dto.CategoryResponseDTO;
import com.mauro.cashiogram.category.dto.CreateCategoryDTO;
import com.mauro.cashiogram.category.dto.UpdateCategoryDTO;
import com.mauro.cashiogram.category.entity.CategoryEntity;
import com.mauro.cashiogram.category.repository.CategoryRepository;
import com.mauro.cashiogram.exception.ResourceNotFoundException;
import com.mauro.cashiogram.subcategory.repository.SubcategoryRepository;
import com.mauro.cashiogram.transaction.repository.TransactionRepository;

import lombok.RequiredArgsConstructor;

import com.mauro.cashiogram.exception.BadRequestException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final SubcategoryRepository subcategoryRepository;
    private final TransactionRepository transactionRepository;

    public List<CategoryResponseDTO> getCategories(String name) {
        List<CategoryEntity> categories;

        if (name == null || name.isBlank()) {
            categories = categoryRepository.findAll();
        } else {
            categories = categoryRepository.findByNameContainingIgnoreCase(name);
        }

        return categories
                .stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    public CategoryResponseDTO getCategoryById(UUID id) {
        CategoryEntity category = categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "The category id " + id + " was not found"));

        return mapToResponseDTO(category);
    }

    @Transactional
    public CategoryResponseDTO createCategory(CreateCategoryDTO createCategoryDTO) {
        if (categoryRepository.existsByNameIgnoreCase(createCategoryDTO.name())) {
            throw new BadRequestException("Category already exists");
        }

        CategoryEntity newCategory = new CategoryEntity();

        newCategory.setName(createCategoryDTO.name());
        newCategory.setDescription(createCategoryDTO.description());

        CategoryEntity savedCategory = categoryRepository.save(newCategory);

        return this.mapToResponseDTO(savedCategory);
    }

    @Transactional
    public CategoryResponseDTO updateCategory(UUID id, UpdateCategoryDTO updateCategoryDTO) {
        CategoryEntity category = categoryRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException(
                "The category id " + id + " was not found"));

        if (updateCategoryDTO.name() != null) {
            if (updateCategoryDTO.name().isBlank()) {
                throw new BadRequestException("Category name must not be blank");
            }

            if (!category.getName().equalsIgnoreCase(updateCategoryDTO.name())
                    && categoryRepository.existsByNameIgnoreCase(updateCategoryDTO.name())) {
                throw new BadRequestException("Category already exists");
            }

            category.setName(updateCategoryDTO.name());
        }

        if (updateCategoryDTO.description() != null) {
            category.setDescription(updateCategoryDTO.description());
        }

        CategoryEntity updatedCategory = categoryRepository.save(category);

        return this.mapToResponseDTO(updatedCategory);
    }

    @Transactional
    public void deleteCategory(UUID categoryId) {
        CategoryEntity category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        if (transactionRepository.existsByCategoryId(categoryId)) {
            throw new BadRequestException(
                    "Category cannot be deleted because it is used by existing transactions");
        }

        subcategoryRepository.deleteByCategoryId(categoryId);
        categoryRepository.delete(category);
    }

    // Mapper method
    private CategoryResponseDTO mapToResponseDTO(CategoryEntity category) {
        return new CategoryResponseDTO(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getCreatedAt());
    }
}
