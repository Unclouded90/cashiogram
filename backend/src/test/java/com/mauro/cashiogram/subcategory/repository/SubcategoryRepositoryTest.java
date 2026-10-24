package com.mauro.cashiogram.subcategory.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import com.mauro.cashiogram.category.entity.CategoryEntity;
import com.mauro.cashiogram.category.repository.CategoryRepository;
import com.mauro.cashiogram.subcategory.entity.SubcategoryEntity;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SubcategoryRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");

    @Autowired
    private SubcategoryRepository subcategoryRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void shouldFindSubcategoriesByNameIgnoringCase() {
        CategoryEntity groceries = createCategory("Groceries");

        createSubcategory(groceries, "Food");
        createSubcategory(groceries, "Cleaning Products");

        List<SubcategoryEntity> result = subcategoryRepository
                .findByNameContainingIgnoreCase("FOO");

        assertEquals(1, result.size());
        assertEquals("Food", result.get(0).getName());
    }

    @Test
    void shouldFindSubcategoriesByCategory() {
        CategoryEntity groceries = createCategory("Groceries");

        CategoryEntity bills = createCategory("Bills");

        createSubcategory(groceries, "Food");
        createSubcategory(groceries, "Cleaning");
        createSubcategory(bills, "Electricity");

        List<SubcategoryEntity> result = subcategoryRepository
                .findByCategoryId(groceries.getId());

        assertEquals(2, result.size());

        assertTrue(
                result.stream()
                        .allMatch(subcategory -> subcategory.getCategory()
                                .getId()
                                .equals(groceries.getId())));
    }

    @Test
    void shouldFindSubcategoriesByCategoryAndName() {
        CategoryEntity groceries = createCategory("Groceries");

        CategoryEntity bills = createCategory("Bills");

        createSubcategory(groceries, "Other");
        createSubcategory(bills, "Other");
        createSubcategory(groceries, "Food");

        List<SubcategoryEntity> result = subcategoryRepository
                .findByCategoryIdAndNameContainingIgnoreCase(
                        groceries.getId(),
                        "OTHER");

        assertEquals(1, result.size());
        assertEquals("Other", result.get(0).getName());
        assertEquals(
                groceries.getId(),
                result.get(0).getCategory().getId());
    }

    @Test
    void shouldCheckDuplicateInsideCategoryIgnoringCase() {
        CategoryEntity groceries = createCategory("Groceries");

        SubcategoryEntity food = createSubcategory(groceries, "Food");

        assertTrue(
                subcategoryRepository
                        .existsByCategoryIdAndNameIgnoreCase(
                                groceries.getId(),
                                "food"));

        assertFalse(
                subcategoryRepository
                        .existsByCategoryIdAndNameIgnoreCaseAndIdNot(
                                groceries.getId(),
                                "FOOD",
                                food.getId()));
    }

    @Test
    void shouldAllowSameNameInDifferentCategories() {
        CategoryEntity groceries = createCategory("Groceries");

        CategoryEntity bills = createCategory("Bills");

        createSubcategory(groceries, "Other");
        createSubcategory(bills, "Other");

        subcategoryRepository.flush();

        assertEquals(
                2,
                subcategoryRepository
                        .findByNameContainingIgnoreCase("Other")
                        .size());
    }

    @Test
    void shouldNotAllowDuplicateNameInsideSameCategoryIgnoringCase() {
        CategoryEntity groceries = createCategory("Groceries");

        createSubcategory(groceries, "Food");

        SubcategoryEntity duplicate = new SubcategoryEntity();

        duplicate.setName("food");
        duplicate.setCategory(groceries);

        subcategoryRepository.save(duplicate);

        assertThrows(
                DataIntegrityViolationException.class,
                () -> subcategoryRepository.flush());
    }

    // Helper methods

    private CategoryEntity createCategory(String name) {
        CategoryEntity category = new CategoryEntity();
        category.setName(name);

        return categoryRepository.save(category);
    }

    private SubcategoryEntity createSubcategory(
            CategoryEntity category,
            String name) {

        SubcategoryEntity subcategory = new SubcategoryEntity();

        subcategory.setName(name);
        subcategory.setCategory(category);

        return subcategoryRepository.save(subcategory);
    }
}