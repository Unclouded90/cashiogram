package com.mauro.cashiogram.category;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mauro.cashiogram.category.dto.CategoryResponseDTO;
import com.mauro.cashiogram.category.dto.CreateCategoryDTO;
import com.mauro.cashiogram.category.dto.UpdateCategoryDTO;
import com.mauro.cashiogram.category.entity.CategoryEntity;
import com.mauro.cashiogram.category.repository.CategoryRepository;
import com.mauro.cashiogram.exception.BadRequestException;
import com.mauro.cashiogram.exception.ResourceNotFoundException;
import com.mauro.cashiogram.subcategory.repository.SubcategoryRepository;
import com.mauro.cashiogram.transaction.repository.TransactionRepository;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {
    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private SubcategoryRepository subcategoryRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private CategoryService categoryService;

    private CategoryEntity groceries;
    private CategoryEntity restaurants;
    private CategoryEntity fuel;
    private CategoryEntity bills;

    @BeforeEach
    void setup() {
        groceries = new CategoryEntity();
        groceries.setId(UUID.randomUUID());
        groceries.setName("Groceries");
        groceries.setDescription("General monthly purchases");

        restaurants = new CategoryEntity();
        restaurants.setId(UUID.randomUUID());
        restaurants.setName("Restaurants");
        restaurants.setDescription("Eating outside or ordering takeaway");
    }

    @Test
    void itShouldReturnAllCategories() {
        when(categoryRepository.findAll())
                .thenReturn(List.of(groceries, restaurants));

        List<CategoryResponseDTO> result = categoryService.getCategories(null);

        assertEquals(2, result.size());

        verify(categoryRepository).findAll();
        verify(categoryRepository, never()).findByNameContainingIgnoreCase(anyString());
    }

    @Test
    void itShouldReturnAllCategoriesWithGroceriesName() {
        when(categoryRepository.findByNameContainingIgnoreCase("groceries"))
                .thenReturn(List.of(groceries));

        List<CategoryResponseDTO> result = categoryService.getCategories("groceries");

        assertEquals(1, result.size());
        assertEquals("Groceries", result.get(0).name());

        verify(categoryRepository).findByNameContainingIgnoreCase("groceries");
        verify(categoryRepository, never()).findAll();
    }

    @Test
    void itShouldReturnCategoryById() {
        UUID id = groceries.getId();

        when(categoryRepository.findById(id))
                .thenReturn(Optional.of(groceries));

        CategoryResponseDTO result = categoryService.getCategoryById(id);

        assertEquals("Groceries", result.name());

        verify(categoryRepository).findById(id);
    }

    @Test
    void itShouldFailToFindCategoryById() {
        UUID id = UUID.randomUUID();

        when(categoryRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> categoryService.getCategoryById(id));

        verify(categoryRepository).findById(id);
    }

    @Test
    void itShouldCreateCategory() {
        CreateCategoryDTO request = new CreateCategoryDTO(
                "Fuel",
                "Fuel expenses");

        fuel = new CategoryEntity();
        fuel.setName(request.name());
        fuel.setDescription(request.description());

        when(categoryRepository.save(any(CategoryEntity.class)))
                .thenReturn(fuel);

        CategoryResponseDTO result = categoryService.createCategory(request);

        assertEquals(fuel.getId(), result.id());
        assertEquals(fuel.getName(), result.name());
        assertEquals(fuel.getDescription(), result.description());
        assertEquals(fuel.getCreatedAt(), result.createdAt());

        verify(categoryRepository).existsByNameIgnoreCase(request.name());
    }

    @Test
    void itShouldFailToCreateCategoryWhenNameAlreadyExists() {
        CreateCategoryDTO request = new CreateCategoryDTO(
                "Groceries",
                "Random purchases");

        when(categoryRepository.existsByNameIgnoreCase("Groceries"))
                .thenReturn(true);

        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> categoryService.createCategory(request));

        assertEquals("Category already exists", exception.getMessage());

        verify(categoryRepository).existsByNameIgnoreCase("Groceries");
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void itShouldUpdateCategory() {
        UUID id = UUID.randomUUID();

        bills = new CategoryEntity();
        bills.setName("Bills");
        bills.setDescription("Monthly bills");

        UpdateCategoryDTO request = new UpdateCategoryDTO(
                "Updated with success", null);

        when(categoryRepository.findById(id))
                .thenReturn(Optional.of(bills));

        when(categoryRepository.save(bills))
                .thenReturn(bills);

        CategoryResponseDTO result = categoryService.updateCategory(id, request);

        assertEquals(request.name(), result.name());
        assertEquals(bills.getDescription(), result.description());

        verify(categoryRepository).findById(id);
        verify(categoryRepository).save(bills);
    }

    @Test
    void itShouldFailToUpdateCategoryIfNotFound() {
        UUID id = UUID.randomUUID();

        UpdateCategoryDTO request = new UpdateCategoryDTO(
                "Update", null);

        when(categoryRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> categoryService.updateCategory(id, request));

        verify(categoryRepository, never()).save(any(CategoryEntity.class));
    }

    @Test
    void itShouldFailToUpdateCategoryIfNameIsBlank() {
        UUID id = UUID.randomUUID();

        UpdateCategoryDTO request = new UpdateCategoryDTO(
                " ", null);

        when(categoryRepository.findById(id))
                .thenReturn(Optional.of(groceries));

        assertThrows(BadRequestException.class,
                () -> categoryService.updateCategory(id, request));

        verify(categoryRepository, never()).save(any(CategoryEntity.class));
    }

    @Test
    void itShouldFailToUpdateCategoryIfNameIsDuplicated() {
        UUID id = groceries.getId();

        UpdateCategoryDTO request = new UpdateCategoryDTO(
                "Restaurants", null);

        when(categoryRepository.findById(id))
                .thenReturn(Optional.of(groceries));

        when(categoryRepository.existsByNameIgnoreCase("Restaurants"))
                .thenReturn(true);

        assertThrows(BadRequestException.class,
                () -> categoryService.updateCategory(id, request));

        verify(categoryRepository, never()).save(any(CategoryEntity.class));
    }

    @Test
    void itShouldDeleteCategory() {
        UUID id = groceries.getId();

        when(categoryRepository.findById(id))
                .thenReturn(Optional.of(groceries));

        when(transactionRepository.existsByCategoryId(id))
                .thenReturn(false);

        categoryService.deleteCategory(id);

        verify(categoryRepository).findById(id);
        verify(transactionRepository).existsByCategoryId(id);
        verify(subcategoryRepository).deleteByCategoryId(id);
        verify(categoryRepository).delete(groceries);
    }

    @Test
    void itShouldFailToDeleteCategoryIfNotFound() {
        UUID id = UUID.randomUUID();

        when(categoryRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> categoryService.deleteCategory(id));

        verify(categoryRepository).findById(id);

        verify(transactionRepository, never())
                .existsByCategoryId(any());

        verify(subcategoryRepository, never())
                .deleteByCategoryId(any());

        verify(categoryRepository, never())
                .delete(any(CategoryEntity.class));
    }

    @Test
    void itShouldFailToDeleteCategoryIfUsedByTransaction() {
        UUID id = groceries.getId();

        when(categoryRepository.findById(id))
                .thenReturn(Optional.of(groceries));

        when(transactionRepository.existsByCategoryId(id))
                .thenReturn(true);

        assertThrows(
                BadRequestException.class,
                () -> categoryService.deleteCategory(id));

        verify(categoryRepository).findById(id);
        verify(transactionRepository).existsByCategoryId(id);

        verify(subcategoryRepository, never())
                .deleteByCategoryId(any());

        verify(categoryRepository, never())
                .delete(any(CategoryEntity.class));
    }
}
