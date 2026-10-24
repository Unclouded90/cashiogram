package com.mauro.cashiogram.category.repository;

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

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CategoryRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void shouldFindCategoriesByNameIgnoringCase() {
        createCategory("Groceries");
        createCategory("Restaurants");

        List<CategoryEntity> result = categoryRepository
                .findByNameContainingIgnoreCase("GROC");

        assertEquals(1, result.size());
        assertEquals("Groceries", result.get(0).getName());
    }

    @Test
    void shouldCheckIfCategoryNameExistsIgnoringCase() {
        createCategory("Groceries");

        assertTrue(
                categoryRepository
                        .existsByNameIgnoreCase("gRoCeRiEs"));

        assertFalse(
                categoryRepository
                        .existsByNameIgnoreCase("Bills"));
    }

    @Test
    void shouldNotAllowDuplicateCategoryNamesIgnoringCase() {
        createCategory("Groceries");

        CategoryEntity duplicate = new CategoryEntity();
        duplicate.setName("groceries");

        categoryRepository.save(duplicate);

        assertThrows(
                DataIntegrityViolationException.class,
                () -> categoryRepository.flush());
    }

    // Helper methods

    private CategoryEntity createCategory(String name) {
        CategoryEntity category = new CategoryEntity();
        category.setName(name);

        return categoryRepository.save(category);
    }
}