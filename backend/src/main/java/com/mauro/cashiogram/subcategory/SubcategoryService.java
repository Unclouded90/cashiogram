package com.mauro.cashiogram.subcategory;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mauro.cashiogram.category.entity.CategoryEntity;
import com.mauro.cashiogram.category.repository.CategoryRepository;
import com.mauro.cashiogram.exception.ResourceNotFoundException;
import com.mauro.cashiogram.exception.BadRequestException;
import com.mauro.cashiogram.subcategory.dto.CreateSubcategoryDTO;
import com.mauro.cashiogram.subcategory.dto.SubcategoryResponseDTO;
import com.mauro.cashiogram.subcategory.dto.UpdateSubcategoryDTO;
import com.mauro.cashiogram.subcategory.entity.SubcategoryEntity;
import com.mauro.cashiogram.subcategory.repository.SubcategoryRepository;
import com.mauro.cashiogram.transaction.repository.TransactionSplitRepository;

@Service
@Transactional(readOnly = true)
public class SubcategoryService {
    private final CategoryRepository categoryRepository;
    private final SubcategoryRepository subcategoryRepository;
    private final TransactionSplitRepository transactionSplitRepository;

    SubcategoryService(SubcategoryRepository subcategoryRepository, CategoryRepository categoryRepository,
            TransactionSplitRepository transactionSplitRepository) {
        this.subcategoryRepository = subcategoryRepository;
        this.categoryRepository = categoryRepository;
        this.transactionSplitRepository = transactionSplitRepository;
    }

    public List<SubcategoryResponseDTO> getSubcategories(UUID categoryId, String name) {
        List<SubcategoryEntity> subcategories;
        boolean hasName = name != null && !name.isBlank();

        if (categoryId != null && hasName) {
            subcategories = subcategoryRepository.findByCategoryIdAndNameContainingIgnoreCase(categoryId, name);
        } else if (categoryId != null && !hasName) {
            subcategories = subcategoryRepository.findByCategoryId(categoryId);
        } else if (categoryId == null && hasName) {
            subcategories = subcategoryRepository.findByNameContainingIgnoreCase(name);
        } else {
            subcategories = subcategoryRepository.findAll();
        }

        return subcategories
                .stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    public SubcategoryResponseDTO getSubcategoryById(UUID id) {
        SubcategoryEntity subcategory = subcategoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "The Subcategory id " + id + " was not found"));

        return mapToResponseDTO(subcategory);
    }

    @Transactional
    public SubcategoryResponseDTO createSubcategory(UUID categoryId, CreateSubcategoryDTO createSubcategoryDTO) {
        CategoryEntity category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("The category id " + categoryId + " was not found"));

        if (subcategoryRepository.existsByCategoryIdAndNameIgnoreCase(categoryId, createSubcategoryDTO.name())) {
            throw new BadRequestException("Subcategory already exists in that category");
        }

        SubcategoryEntity newSubCategory = new SubcategoryEntity();

        newSubCategory.setName(createSubcategoryDTO.name());
        newSubCategory.setDescription(createSubcategoryDTO.description());
        newSubCategory.setCategory(category);

        SubcategoryEntity savedSubcategory = subcategoryRepository.save(newSubCategory);

        return mapToResponseDTO(savedSubcategory);
    }

    @Transactional
    public SubcategoryResponseDTO updateSubcategory(UUID subcategoryId, UpdateSubcategoryDTO updateSubcategoryDTO) {
        SubcategoryEntity subcategory = subcategoryRepository.findById(subcategoryId).orElseThrow(
                () -> new ResourceNotFoundException("The subcategory id " + subcategoryId + " was not found"));

        CategoryEntity category = subcategory.getCategory();

        if (updateSubcategoryDTO.categoryId() != null) {
            category = categoryRepository.findById(updateSubcategoryDTO.categoryId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "The category id " + updateSubcategoryDTO.categoryId() + " was not found"));
        }

        if (updateSubcategoryDTO.name() != null) {
            if (updateSubcategoryDTO.name().isBlank()) {
                throw new BadRequestException("Subcategory name must not be blank");
            }

            subcategory.setName(updateSubcategoryDTO.name());
        }

        if (subcategoryRepository.existsByCategoryIdAndNameIgnoreCaseAndIdNot(
                category.getId(),
                subcategory.getName(),
                subcategoryId)) {
            throw new BadRequestException("Subcategory already exists in that category");
        }

        subcategory.setCategory(category);

        if (updateSubcategoryDTO.description() != null) {
            subcategory.setDescription(updateSubcategoryDTO.description());
        }

        SubcategoryEntity updatedSubcategory = subcategoryRepository.save(subcategory);

        return mapToResponseDTO(updatedSubcategory);
    }

    @Transactional
    public void deleteSubcategory(UUID subcategoryId) {
        SubcategoryEntity subcategory = subcategoryRepository.findById(subcategoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Subcategory not found"));

        if (transactionSplitRepository.existsBySubcategoryId(subcategoryId)) {
            throw new BadRequestException(
                    "Subcategory cannot be deleted because it is used by existing transaction splits");
        }

        subcategoryRepository.delete(subcategory);
    }

    // Mapper method
    private SubcategoryResponseDTO mapToResponseDTO(SubcategoryEntity subcategory) {
        return new SubcategoryResponseDTO(
                subcategory.getId(),
                subcategory.getName(),
                subcategory.getDescription(),
                subcategory.getCategory().getId(),
                subcategory.getCreatedAt());
    }
}
