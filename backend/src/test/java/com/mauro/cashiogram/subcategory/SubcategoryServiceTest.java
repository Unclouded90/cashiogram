package com.mauro.cashiogram.subcategory;

import java.util.UUID;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mauro.cashiogram.category.entity.CategoryEntity;
import com.mauro.cashiogram.category.repository.CategoryRepository;
import com.mauro.cashiogram.exception.ResourceNotFoundException;
import com.mauro.cashiogram.subcategory.dto.SubcategoryResponseDTO;
import com.mauro.cashiogram.subcategory.entity.SubcategoryEntity;
import com.mauro.cashiogram.subcategory.repository.SubcategoryRepository;

@ExtendWith(MockitoExtension.class)
public class SubcategoryServiceTest {
    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private SubcategoryRepository subcategoryRepository;

    @InjectMocks 
    private SubcategoryService subcategoryService;

    private CategoryEntity groceries;
    private SubcategoryEntity cats;
    private SubcategoryEntity food;
    private SubcategoryEntity cleaning;
    private SubcategoryEntity extras;

    @BeforeEach 
    void setup() {
        groceries = new CategoryEntity();
        groceries.setId(UUID.randomUUID());
        groceries.setName("Groceries");
        groceries.setDescription("General monthly purchases");

        cats = new SubcategoryEntity();
        cats.setId(UUID.randomUUID());
        cats.setName("Cats");
        cats.setDescription("Cat related purchases");
        cats.setCategory(groceries);

        food = new SubcategoryEntity();
        food.setId(UUID.randomUUID());
        food.setName("Food");
        food.setDescription("Food related purchases");
        food.setCategory(groceries);
    }

    @Test 
    void itShouldReturnAllSubcategories() {
        when(subcategoryRepository.findAll())
            .thenReturn(List.of(cats, food));

        List<SubcategoryResponseDTO> result = subcategoryService.getSubcategories(null, null);
        
        assertEquals(2, result.size());

        verify(subcategoryRepository).findAll();
        verify(subcategoryRepository, never())
            .findByCategoryIdAndNameContainingIgnoreCase(any(UUID.class), anyString());
        verify(subcategoryRepository, never()).findByCategoryId(any(UUID.class));
        verify(subcategoryRepository, never()).findByNameContainingIgnoreCase(anyString());
    }

    @Test 
    void itShouldReturnAllSubcategoriesInACategory() {
        UUID id = groceries.getId();

        when(subcategoryRepository.findByCategoryId(id))
            .thenReturn(List.of(cats, food));

        List<SubcategoryResponseDTO> result = subcategoryService.getSubcategories(id, null);
    
        assertEquals(2, result.size());

        verify(subcategoryRepository).findByCategoryId(id);
        verify(subcategoryRepository, never())
            .findByCategoryIdAndNameContainingIgnoreCase(any(UUID.class), anyString());
        verify(subcategoryRepository, never()).findAll();
        verify(subcategoryRepository, never()).findByNameContainingIgnoreCase(anyString());
    }

    @Test 
    void itShouldReturnAllSubcategoriesWithFoodName() {
        when(subcategoryRepository.findByNameContainingIgnoreCase("food"))
        .thenReturn(List.of(food));

        List<SubcategoryResponseDTO> result = subcategoryService.getSubcategories(null, "food");

        assertEquals(1, result.size());

        verify(subcategoryRepository).findByNameContainingIgnoreCase("food");
        verify(subcategoryRepository, never())
            .findByCategoryIdAndNameContainingIgnoreCase(any(UUID.class), anyString());
        verify(subcategoryRepository, never()).findAll();
        verify(subcategoryRepository, never()).findByCategoryId(any(UUID.class));
    }

    @Test 
    void itShouldReturnAllSubcategoriesInGroceriesCategoryWithFoodName() {
        UUID id = groceries.getId();

        when(subcategoryRepository.findByCategoryIdAndNameContainingIgnoreCase(id, "food"))
            .thenReturn(List.of(food));

        List<SubcategoryResponseDTO> result = subcategoryService.getSubcategories(id, "food");

        assertEquals(1, result.size());

        verify(subcategoryRepository).findByCategoryIdAndNameContainingIgnoreCase(id, "food");
        verify(subcategoryRepository, never())
            .findByNameContainingIgnoreCase(anyString());
        verify(subcategoryRepository, never()).findAll();
        verify(subcategoryRepository, never()).findByCategoryId(any(UUID.class));
    }

    @Test 
    void itShouldReturnSubcategoryById() {
        UUID id = cats.getId();

        when(subcategoryRepository.findById(id))
            .thenReturn(Optional.of(cats));

        SubcategoryResponseDTO result = subcategoryService.getSubcategoryById(id);

        assertEquals("Cats", result.name());
        assertEquals(cats.getCategory().getId(), result.categoryId());

        verify(subcategoryRepository).findById(id);
    }

    @Test
    void itShouldFailToFindSubcategoryById() {
        UUID id = UUID.randomUUID();

        when(subcategoryRepository.findById(id))
            .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
            () -> subcategoryService.getSubcategoryById(id)
        );

        verify(subcategoryRepository).findById(id);
    }

    @Test 
    void itShouldCreateSubcategory() {
        
    }
}
