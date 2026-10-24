package com.mauro.cashiogram.report.repository;

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
import com.mauro.cashiogram.report.dto.CategoryTotalDTO;
import com.mauro.cashiogram.report.dto.SubcategoryTotalDTO;
import com.mauro.cashiogram.subcategory.entity.SubcategoryEntity;
import com.mauro.cashiogram.subcategory.repository.SubcategoryRepository;
import com.mauro.cashiogram.transaction.entity.TransactionEntity;
import com.mauro.cashiogram.transaction.entity.TransactionSplitEntity;
import com.mauro.cashiogram.transaction.entity.TransactionType;
import com.mauro.cashiogram.transaction.repository.TransactionRepository;
import com.mauro.cashiogram.transaction.repository.TransactionSplitRepository;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ReportRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18");

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SubcategoryRepository subcategoryRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TransactionSplitRepository transactionSplitRepository;

    @Test
    void shouldSumAmountByTypeAndPeriod() {
        CategoryEntity category = createCategory("Salary");

        createTransaction(
                category,
                new BigDecimal("2000.00"),
                TransactionType.INCOME,
                LocalDate.of(2026, 10, 5));

        createTransaction(
                category,
                new BigDecimal("500.00"),
                TransactionType.INCOME,
                LocalDate.of(2026, 10, 15));

        createTransaction(
                category,
                new BigDecimal("999.00"),
                TransactionType.INCOME,
                LocalDate.of(2026, 9, 30));

        createTransaction(
                category,
                new BigDecimal("300.00"),
                TransactionType.EXPENSE,
                LocalDate.of(2026, 10, 10));

        BigDecimal total = reportRepository.sumAmountByTypeAndPeriod(
                TransactionType.INCOME,
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31));

        assertEquals(
                0,
                new BigDecimal("2500.00")
                        .compareTo(total));
    }

    @Test
    void shouldGetTotalsByCategory() {
        CategoryEntity groceries = createCategory("Groceries");

        CategoryEntity bills = createCategory("Bills");

        createTransaction(
                groceries,
                new BigDecimal("100.00"),
                TransactionType.EXPENSE,
                LocalDate.of(2026, 10, 5));

        createTransaction(
                groceries,
                new BigDecimal("150.00"),
                TransactionType.EXPENSE,
                LocalDate.of(2026, 10, 10));

        createTransaction(
                bills,
                new BigDecimal("180.00"),
                TransactionType.EXPENSE,
                LocalDate.of(2026, 10, 15));

        List<CategoryTotalDTO> result = reportRepository.getTotalsByCategory(
                TransactionType.EXPENSE,
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31));

        assertEquals(2, result.size());

        assertEquals(
                groceries.getId(),
                result.get(0).categoryId());

        assertEquals(
                "Groceries",
                result.get(0).categoryName());

        assertEquals(
                0,
                new BigDecimal("250.00")
                        .compareTo(result.get(0).total()));

        assertEquals(
                bills.getId(),
                result.get(1).categoryId());

        assertEquals(
                0,
                new BigDecimal("180.00")
                        .compareTo(result.get(1).total()));
    }

    @Test
    void shouldGetTotalsBySubcategory() {
        CategoryEntity groceries = createCategory("Groceries");

        SubcategoryEntity food = createSubcategory(
                groceries,
                "Food");

        SubcategoryEntity catProducts = createSubcategory(
                groceries,
                "Cat Products");

        TransactionEntity firstTransaction = createTransaction(
                groceries,
                new BigDecimal("100.00"),
                TransactionType.EXPENSE,
                LocalDate.of(2026, 10, 5));

        TransactionEntity secondTransaction = createTransaction(
                groceries,
                new BigDecimal("80.00"),
                TransactionType.EXPENSE,
                LocalDate.of(2026, 10, 10));

        createSplit(
                firstTransaction,
                food,
                new BigDecimal("60.00"));

        createSplit(
                firstTransaction,
                catProducts,
                new BigDecimal("20.00"));

        createSplit(
                secondTransaction,
                food,
                new BigDecimal("40.00"));

        List<SubcategoryTotalDTO> result = reportRepository.getTotalsBySubcategory(
                TransactionType.EXPENSE,
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31));

        assertEquals(2, result.size());

        assertEquals(
                food.getId(),
                result.get(0).subcategoryId());

        assertEquals(
                "Food",
                result.get(0).subcategoryName());

        assertEquals(
                0,
                new BigDecimal("100.00")
                        .compareTo(result.get(0).total()));

        assertEquals(
                catProducts.getId(),
                result.get(1).subcategoryId());

        assertEquals(
                0,
                new BigDecimal("20.00")
                        .compareTo(result.get(1).total()));
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
            CategoryEntity category,
            BigDecimal amount,
            TransactionType transactionType,
            LocalDate date) {

        TransactionEntity transaction = new TransactionEntity();

        transaction.setAmount(amount);
        transaction.setTransactionType(transactionType);
        transaction.setCategory(category);
        transaction.setDate(date);
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