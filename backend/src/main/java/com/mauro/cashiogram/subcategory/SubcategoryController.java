package com.mauro.cashiogram.subcategory;

import org.springframework.web.bind.annotation.RestController;

import com.mauro.cashiogram.subcategory.dto.CreateSubcategoryDTO;
import com.mauro.cashiogram.subcategory.dto.SubcategoryResponseDTO;
import com.mauro.cashiogram.subcategory.dto.UpdateSubcategoryDTO;

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
public class SubcategoryController {
    private final SubcategoryService subcategoryService;

    public SubcategoryController(SubcategoryService subcategoryService) {
        this.subcategoryService = subcategoryService;
    }

    @GetMapping("/subcategories")
    public ResponseEntity<List<SubcategoryResponseDTO>> getSubcategories(
            @RequestParam(name = "categoryId", required = false) UUID categoryId,
            @RequestParam(name = "name", required = false) String name) {
        return ResponseEntity.ok(subcategoryService.getSubcategories(categoryId, name));
    }
    
    @GetMapping("/subcategories/{subcategoryId}")
    public ResponseEntity<SubcategoryResponseDTO> getSubcategoryById(
            @PathVariable("subcategoryId") UUID subcategoryId) {
        return ResponseEntity.ok(subcategoryService.getSubcategoryById(subcategoryId));
    }
    
    @PostMapping("/categories/{categoryId}/subcategories")
    public ResponseEntity<SubcategoryResponseDTO> createSubcategory(
            @PathVariable("categoryId") UUID categoryId,
            @Valid @RequestBody CreateSubcategoryDTO createSubcategoryDTO) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(subcategoryService.createSubcategory(categoryId, createSubcategoryDTO));
    }
    
    @PatchMapping("/subcategories/{subcategoryId}")
    public ResponseEntity<SubcategoryResponseDTO> updateSubcategory(
            @PathVariable("subcategoryId") UUID subcategoryId,
            @Valid @RequestBody UpdateSubcategoryDTO updateSubcategoryDTO) {
        return ResponseEntity.ok(subcategoryService.updateSubcategory(subcategoryId, updateSubcategoryDTO));
    }

    @DeleteMapping("/subcategories/{subcategoryId}")
    public ResponseEntity<Void> deleteSubcategory(@PathVariable("subcategoryId") UUID subcategoryId) {
        subcategoryService.deleteSubcategory(subcategoryId);

        return ResponseEntity.noContent().build();
    }
}
