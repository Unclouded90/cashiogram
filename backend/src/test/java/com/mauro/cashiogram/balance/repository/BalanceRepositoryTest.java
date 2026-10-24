package com.mauro.cashiogram.balance.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDate;

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
import com.mauro.cashiogram.transaction.repository.TransactionRepository;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BalanceRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");

    @Autowired
    private BalanceRepository balanceRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Test
    void shouldSumTransactionsByType() {
        CategoryEntity category = new CategoryEntity();
        category.setName("Test Category");

        CategoryEntity savedCategory = categoryRepository.save(category);

        TransactionEntity income1 = createTransaction(
                savedCategory,
                new BigDecimal("1000.00"),
                TransactionType.INCOME);

        TransactionEntity income2 = createTransaction(
                savedCategory,
                new BigDecimal("250.00"),
                TransactionType.INCOME);

        TransactionEntity expense = createTransaction(
                savedCategory,
                new BigDecimal("400.00"),
                TransactionType.EXPENSE);

        transactionRepository.save(income1);
        transactionRepository.save(income2);
        transactionRepository.save(expense);

        BigDecimal totalIncome = balanceRepository.sumAmountByTransactionType(
                TransactionType.INCOME);

        BigDecimal totalExpenses = balanceRepository.sumAmountByTransactionType(
                TransactionType.EXPENSE);

        assertEquals(
                0,
                new BigDecimal("1250.00")
                        .compareTo(totalIncome));

        assertEquals(
                0,
                new BigDecimal("400.00")
                        .compareTo(totalExpenses));
    }

    private TransactionEntity createTransaction(
            CategoryEntity category,
            BigDecimal amount,
            TransactionType transactionType) {

        TransactionEntity transaction = new TransactionEntity();

        transaction.setAmount(amount);
        transaction.setTransactionType(transactionType);
        transaction.setCategory(category);
        transaction.setDate(LocalDate.now());
        transaction.setDescription("Repository test");

        return transaction;
    }
}