package com.mauro.cashiogram.transaction.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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
import com.mauro.cashiogram.subcategory.repository.SubcategoryRepository;
import com.mauro.cashiogram.transaction.entity.TransactionEntity;
import com.mauro.cashiogram.transaction.entity.TransactionSplitEntity;
import com.mauro.cashiogram.transaction.entity.TransactionType;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TransactionSplitRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");

    @Autowired
    private TransactionSplitRepository transactionSplitRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SubcategoryRepository subcategoryRepository;

    @Test
    void shouldFindSplitsByTransactionId() {
        CategoryEntity category = createCategory("Groceries");

        SubcategoryEntity food = createSubcategory(category, "Food");

        SubcategoryEntity cats = createSubcategory(category, "Cat Products");

        TransactionEntity transaction = createTransaction(category);

        createSplit(
                transaction,
                food,
                new BigDecimal("60.00"));

        createSplit(
                transaction,
                cats,
                new BigDecimal("20.00"));

        List<TransactionSplitEntity> result = transactionSplitRepository
                .findByTransactionId(transaction.getId());

        assertEquals(2, result.size());

        assertTrue(
                result.stream()
                        .allMatch(split -> split.getTransaction()
                                .getId()
                                .equals(transaction.getId())));
    }

    @Test
    void shouldFindSplitByIdAndTransactionId() {
        CategoryEntity category = createCategory("Groceries");

        SubcategoryEntity food = createSubcategory(category, "Food");

        TransactionEntity transaction = createTransaction(category);

        TransactionSplitEntity split = createSplit(
                transaction,
                food,
                new BigDecimal("60.00"));

        Optional<TransactionSplitEntity> result = transactionSplitRepository
                .findByIdAndTransactionId(
                        split.getId(),
                        transaction.getId());

        assertTrue(result.isPresent());
        assertEquals(split.getId(), result.get().getId());
    }

    @Test
    void shouldNotFindSplitUsingDifferentTransactionId() {
        CategoryEntity category = createCategory("Groceries");

        SubcategoryEntity food = createSubcategory(category, "Food");

        TransactionEntity transaction = createTransaction(category);

        TransactionEntity differentTransaction = createTransaction(category);

        TransactionSplitEntity split = createSplit(
                transaction,
                food,
                new BigDecimal("60.00"));

        Optional<TransactionSplitEntity> result = transactionSplitRepository
                .findByIdAndTransactionId(
                        split.getId(),
                        differentTransaction.getId());

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldCheckIfSubcategoryAlreadyExistsInTransaction() {
        CategoryEntity category = createCategory("Groceries");

        SubcategoryEntity food = createSubcategory(category, "Food");

        TransactionEntity transaction = createTransaction(category);

        createSplit(
                transaction,
                food,
                new BigDecimal("60.00"));

        assertTrue(
                transactionSplitRepository
                        .existsByTransactionIdAndSubcategoryId(
                                transaction.getId(),
                                food.getId()));

        assertFalse(
                transactionSplitRepository
                        .existsByTransactionIdAndSubcategoryId(
                                transaction.getId(),
                                UUID.randomUUID()));
    }

    @Test
    void shouldIgnoreCurrentSplitWhenCheckingDuplicate() {
        CategoryEntity category = createCategory("Groceries");

        SubcategoryEntity food = createSubcategory(category, "Food");

        TransactionEntity transaction = createTransaction(category);

        TransactionSplitEntity split = createSplit(
                transaction,
                food,
                new BigDecimal("60.00"));

        assertFalse(
                transactionSplitRepository
                        .existsByTransactionIdAndSubcategoryIdAndIdNot(
                                transaction.getId(),
                                food.getId(),
                                split.getId()));
    }

    @Test
    void shouldDetectAnotherSplitWithSameSubcategory() {
        CategoryEntity category = createCategory("Groceries");

        SubcategoryEntity food = createSubcategory(category, "Food");

        TransactionEntity firstTransaction = createTransaction(category);

        TransactionEntity secondTransaction = createTransaction(category);

        TransactionSplitEntity firstSplit = createSplit(
                firstTransaction,
                food,
                new BigDecimal("60.00"));

        TransactionSplitEntity secondSplit = createSplit(
                secondTransaction,
                food,
                new BigDecimal("30.00"));

        assertTrue(
                transactionSplitRepository
                        .existsByTransactionIdAndSubcategoryIdAndIdNot(
                                secondTransaction.getId(),
                                food.getId(),
                                firstSplit.getId()));

        assertFalse(
                transactionSplitRepository
                        .existsByTransactionIdAndSubcategoryIdAndIdNot(
                                secondTransaction.getId(),
                                food.getId(),
                                secondSplit.getId()));
    }

    @Test
    void shouldDeleteSplitsByTransactionId() {
        CategoryEntity category = createCategory("Groceries");

        SubcategoryEntity food = createSubcategory(category, "Food");

        SubcategoryEntity cats = createSubcategory(category, "Cat Products");

        TransactionEntity transaction = createTransaction(category);

        createSplit(
                transaction,
                food,
                new BigDecimal("60.00"));

        createSplit(
                transaction,
                cats,
                new BigDecimal("20.00"));

        transactionSplitRepository
                .deleteByTransactionId(transaction.getId());

        transactionSplitRepository.flush();

        assertTrue(
                transactionSplitRepository
                        .findByTransactionId(transaction.getId())
                        .isEmpty());
    }

    @Test
    void shouldNotAllowSameSubcategoryTwiceInSameTransaction() {
        CategoryEntity category = createCategory("Groceries");

        SubcategoryEntity food = createSubcategory(category, "Food");

        TransactionEntity transaction = createTransaction(category);

        createSplit(
                transaction,
                food,
                new BigDecimal("60.00"));

        TransactionSplitEntity duplicate = new TransactionSplitEntity();

        duplicate.setTransaction(transaction);
        duplicate.setSubcategory(food);
        duplicate.setAmount(new BigDecimal("20.00"));

        transactionSplitRepository.save(duplicate);

        assertThrows(
                DataIntegrityViolationException.class,
                () -> transactionSplitRepository.flush());
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

    private TransactionEntity createTransaction(
            CategoryEntity category) {

        TransactionEntity transaction = new TransactionEntity();

        transaction.setAmount(
                new BigDecimal("100.00"));
        transaction.setTransactionType(
                TransactionType.EXPENSE);
        transaction.setCategory(category);
        transaction.setDate(LocalDate.now());
        transaction.setDescription("Repository test");

        return transactionRepository.save(transaction);
    }

    private TransactionSplitEntity createSplit(
            TransactionEntity transaction,
            SubcategoryEntity subcategory,
            BigDecimal amount) {

        TransactionSplitEntity split = new TransactionSplitEntity();

        split.setTransaction(transaction);
        split.setSubcategory(subcategory);
        split.setAmount(amount);

        return transactionSplitRepository.save(split);
    }
}