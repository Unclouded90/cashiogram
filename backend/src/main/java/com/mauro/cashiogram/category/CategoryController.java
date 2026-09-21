package com.mauro.cashiogram.category;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mauro.cashiogram.category.dto.CategoryResponseDTO;
import com.mauro.cashiogram.category.dto.CreateCategoryDTO;
import com.mauro.cashiogram.category.dto.UpdateCategoryDTO;

import jakarta.validation.Valid;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;



@RestController
@RequestMapping("/categories")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ResponseEntity<List<CategoryResponseDTO>> getCategories(
            @RequestParam(name = "name", required = false) String name) {
        return ResponseEntity.ok(categoryService.getCategories(name));
    }
    
    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryResponseDTO> getCategoryById(@PathVariable("categoryId") UUID categoryId) {
        return ResponseEntity.ok(categoryService.getCategoryById(categoryId));
    }
    
    @PostMapping
    public ResponseEntity<CategoryResponseDTO> createCategory(
        @Valid @RequestBody CreateCategoryDTO newCategory) {
        
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(categoryService.createCategory(newCategory));
    }
    
    @PatchMapping("/{categoryId}")
    public ResponseEntity<CategoryResponseDTO> updateCategory(
        @PathVariable("categoryId") UUID categoryId,
        @Valid @RequestBody UpdateCategoryDTO updateCategoryDTO) {

        return ResponseEntity.ok(categoryService.updateCategory(categoryId, updateCategoryDTO));
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> deleteCategory(@PathVariable("categoryId") UUID categoryId) {
        categoryService.deleteCategory(categoryId);

        return ResponseEntity.noContent().build();
    }
}
