package com.mauro.cashiogram.transaction.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import com.mauro.cashiogram.category.entity.CategoryEntity;
import com.mauro.cashiogram.category.repository.CategoryRepository;
import com.mauro.cashiogram.transaction.entity.TransactionEntity;
import com.mauro.cashiogram.transaction.entity.TransactionType;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class TransactionRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void shouldFindAllTransactionsOrderedByDateDescending() {
        CategoryEntity category = createCategory("Groceries");

        createTransaction(
                category,
                new BigDecimal("100.00"),
                LocalDate.of(2026, 10, 10));

        createTransaction(
                category,
                new BigDecimal("200.00"),
                LocalDate.of(2026, 10, 20));

        createTransaction(
                category,
                new BigDecimal("50.00"),
                LocalDate.of(2026, 10, 15));

        List<TransactionEntity> result = transactionRepository
                .findAllByOrderByDateDesc();

        assertEquals(3, result.size());

        assertEquals(
                LocalDate.of(2026, 10, 20),
                result.get(0).getDate());

        assertEquals(
                LocalDate.of(2026, 10, 15),
                result.get(1).getDate());

        assertEquals(
                LocalDate.of(2026, 10, 10),
                result.get(2).getDate());
    }

    // Helper methods

    private CategoryEntity createCategory(String name) {
        CategoryEntity category = new CategoryEntity();
        category.setName(name);

        return categoryRepository.save(category);
    }

    private TransactionEntity createTransaction(
            CategoryEntity category,
            BigDecimal amount,
            LocalDate date) {

        TransactionEntity transaction = new TransactionEntity();

        transaction.setAmount(amount);
        transaction.setTransactionType(
                TransactionType.EXPENSE);
        transaction.setCategory(category);
        transaction.setDate(date);
        transaction.setDescription("Repository test");

        return transactionRepository.save(transaction);
    }
}