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
import com.mauro.cashiogram.exception.BadRequestException;
import com.mauro.cashiogram.exception.ResourceNotFoundException;
import com.mauro.cashiogram.subcategory.dto.CreateSubcategoryDTO;
import com.mauro.cashiogram.subcategory.dto.SubcategoryResponseDTO;
import com.mauro.cashiogram.subcategory.dto.UpdateSubcategoryDTO;
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
                () -> subcategoryService.getSubcategoryById(id));

        verify(subcategoryRepository).findById(id);
    }

    @Test
    void itShouldCreateSubcategory() {
        CreateSubcategoryDTO request = new CreateSubcategoryDTO(
                "Floor cleaning",
                "Floor cleaning products");

        cleaning = new SubcategoryEntity();
        cleaning.setName(request.name());
        cleaning.setDescription(request.description());
        cleaning.setCategory(groceries);

        when(categoryRepository.findById(groceries.getId()))
                .thenReturn(Optional.of(groceries));

        when(subcategoryRepository.save(any(SubcategoryEntity.class)))
                .thenReturn(cleaning);

        SubcategoryResponseDTO result = subcategoryService.createSubcategory(groceries.getId(), request);

        assertEquals(cleaning.getId(), result.id());
        assertEquals(cleaning.getName(), result.name());
        assertEquals(cleaning.getDescription(), result.description());
        assertEquals(cleaning.getCategory().getId(), result.categoryId());

        verify(subcategoryRepository).existsByCategoryIdAndNameIgnoreCase(groceries.getId(), request.name());
    }

    @Test
    void itShouldFailToCreateSubcategoryIfCategoryNotFound() {
        UUID id = UUID.randomUUID();

        CreateSubcategoryDTO request = new CreateSubcategoryDTO(
                "Floor cleaning",
                "Floor cleaning products");

        cleaning = new SubcategoryEntity();
        cleaning.setName(request.name());
        cleaning.setDescription(request.description());

        when(categoryRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> subcategoryService.createSubcategory(id, request));

        verify(subcategoryRepository, never()).save(any(SubcategoryEntity.class));
    }

    @Test
    void itShouldFailToCreateSubcategoryWhenNameAlreadyExistsInCategory() {
        CreateSubcategoryDTO request = new CreateSubcategoryDTO(
                "Food",
                null);

        extras = new SubcategoryEntity();
        extras.setName(request.name());
        extras.setDescription(request.description());
        extras.setCategory(groceries);

        when(categoryRepository.findById(groceries.getId()))
                .thenReturn(Optional.of(groceries));

        when(subcategoryRepository.existsByCategoryIdAndNameIgnoreCase(groceries.getId(), request.name()))
                .thenReturn(true);

        assertThrows(BadRequestException.class,
                () -> subcategoryService.createSubcategory(groceries.getId(), request));

        verify(subcategoryRepository, never()).save(any(SubcategoryEntity.class));
    }

    @Test
    void itShouldUpdateSubcategory() {
        UUID id = cats.getId();

        UpdateSubcategoryDTO request = new UpdateSubcategoryDTO(
                "Cat Products",
                null,
                null);

        when(subcategoryRepository.findById(id))
                .thenReturn(Optional.of(cats));

        when(subcategoryRepository.save(cats))
                .thenReturn(cats);

        SubcategoryResponseDTO result = subcategoryService.updateSubcategory(id, request);

        assertEquals("Cat Products", result.name());
        assertEquals(cats.getDescription(), result.description());
        assertEquals(groceries.getId(), result.categoryId());

        verify(subcategoryRepository).findById(id);
        verify(subcategoryRepository).existsByCategoryIdAndNameIgnoreCaseAndIdNot(
                groceries.getId(),
                "Cat Products",
                id);
        verify(subcategoryRepository).save(cats);
    }

    @Test
    void itShouldUpdateSubcategoryCategory() {
        UUID id = cats.getId();

        CategoryEntity bills = new CategoryEntity();
        bills.setId(UUID.randomUUID());
        bills.setName("Bills");

        UpdateSubcategoryDTO request = new UpdateSubcategoryDTO(
                null,
                null,
                bills.getId());

        when(subcategoryRepository.findById(id))
                .thenReturn(Optional.of(cats));

        when(categoryRepository.findById(bills.getId()))
                .thenReturn(Optional.of(bills));

        when(subcategoryRepository.save(cats))
                .thenReturn(cats);

        SubcategoryResponseDTO result = subcategoryService.updateSubcategory(id, request);

        assertEquals("Cats", result.name());
        assertEquals(bills.getId(), result.categoryId());

        verify(categoryRepository).findById(bills.getId());
        verify(subcategoryRepository).save(cats);
    }

    @Test
    void itShouldFailToUpdateSubcategoryIfNotFound() {
        UUID id = UUID.randomUUID();

        UpdateSubcategoryDTO request = new UpdateSubcategoryDTO(
                "Updated",
                null,
                null);

        when(subcategoryRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> subcategoryService.updateSubcategory(id, request));

        verify(subcategoryRepository).findById(id);
        verify(subcategoryRepository, never())
                .save(any(SubcategoryEntity.class));
    }

    @Test
    void itShouldFailToUpdateSubcategoryIfCategoryNotFound() {
        UUID subcategoryId = cats.getId();
        UUID categoryId = UUID.randomUUID();

        UpdateSubcategoryDTO request = new UpdateSubcategoryDTO(
                null,
                null,
                categoryId);

        when(subcategoryRepository.findById(subcategoryId))
                .thenReturn(Optional.of(cats));

        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> subcategoryService.updateSubcategory(subcategoryId, request));

        verify(categoryRepository).findById(categoryId);
        verify(subcategoryRepository, never())
                .save(any(SubcategoryEntity.class));
    }

    @Test
    void itShouldFailToUpdateSubcategoryIfNameIsBlank() {
        UUID id = cats.getId();

        UpdateSubcategoryDTO request = new UpdateSubcategoryDTO(
                " ",
                null,
                null);

        when(subcategoryRepository.findById(id))
                .thenReturn(Optional.of(cats));

        assertThrows(
                BadRequestException.class,
                () -> subcategoryService.updateSubcategory(id, request));

        verify(subcategoryRepository, never())
                .save(any(SubcategoryEntity.class));
    }

    @Test
    void itShouldFailToUpdateSubcategoryIfNameIsDuplicated() {
        UUID id = cats.getId();

        UpdateSubcategoryDTO request = new UpdateSubcategoryDTO(
                "Food",
                null,
                null);

        when(subcategoryRepository.findById(id))
                .thenReturn(Optional.of(cats));

        when(subcategoryRepository.existsByCategoryIdAndNameIgnoreCaseAndIdNot(
                groceries.getId(),
                "Food",
                id))
                .thenReturn(true);

        assertThrows(
                BadRequestException.class,
                () -> subcategoryService.updateSubcategory(id, request));

        verify(subcategoryRepository)
                .existsByCategoryIdAndNameIgnoreCaseAndIdNot(
                        groceries.getId(),
                        "Food",
                        id);

        verify(subcategoryRepository, never())
                .save(any(SubcategoryEntity.class));
    }

    @Test
    void itShouldDeleteSubcategory() {
        UUID id = cats.getId();

        when(subcategoryRepository.findById(id))
                .thenReturn(Optional.of(cats));

        subcategoryService.deleteSubcategory(id);

        verify(subcategoryRepository).findById(id);
        verify(subcategoryRepository).delete(cats);
    }

    @Test
    void itShouldFailToDeleteSubcategoryIfNotFound() {
        UUID id = UUID.randomUUID();

        when(subcategoryRepository.findById(id))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> subcategoryService.deleteSubcategory(id));

        verify(subcategoryRepository).findById(id);
        verify(subcategoryRepository, never())
                .delete(any(SubcategoryEntity.class));
    }
}
