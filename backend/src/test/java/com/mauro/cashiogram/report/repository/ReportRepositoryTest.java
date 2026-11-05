package com.mauro.cashiogram.report.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

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

    private static final List<UUID> NO_CATEGORY_FILTER = List.of(new UUID(0L, 0L));

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
                LocalDate.of(2026, 10, 31),
                false,
                NO_CATEGORY_FILTER,
                false,
                BigDecimal.ZERO,
                false,
                BigDecimal.ZERO);

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
                LocalDate.of(2026, 10, 31),
                false,
                NO_CATEGORY_FILTER,
                false,
                BigDecimal.ZERO,
                false,
                BigDecimal.ZERO);

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
                LocalDate.of(2026, 10, 31),
                "",
                false,
                NO_CATEGORY_FILTER,
                false,
                BigDecimal.ZERO,
                false,
                BigDecimal.ZERO,
                false,
                BigDecimal.ZERO,
                false,
                BigDecimal.ZERO);

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

        assertEquals(
                groceries.getId(),
                result.get(0).categoryId());

        assertEquals(
                "Groceries",
                result.get(0).categoryName());
    }

    @Test
    void shouldFilterSubcategoriesByNameAcrossCategories() {
        CategoryEntity groceries = createCategory("Groceries");
        CategoryEntity vet = createCategory("Veterinarian Bills");

        SubcategoryEntity groceriesCats = createSubcategory(
                groceries,
                "Cats");

        SubcategoryEntity vetCats = createSubcategory(
                vet,
                "Cats");

        SubcategoryEntity food = createSubcategory(
                groceries,
                "Food");

        TransactionEntity groceriesTransaction = createTransaction(
                groceries,
                new BigDecimal("100.00"),
                TransactionType.EXPENSE,
                LocalDate.of(2026, 10, 5));

        TransactionEntity vetTransaction = createTransaction(
                vet,
                new BigDecimal("90.00"),
                TransactionType.EXPENSE,
                LocalDate.of(2026, 10, 10));

        createSplit(
                groceriesTransaction,
                groceriesCats,
                new BigDecimal("60.00"));

        createSplit(
                groceriesTransaction,
                food,
                new BigDecimal("40.00"));

        createSplit(
                vetTransaction,
                vetCats,
                new BigDecimal("90.00"));

        List<SubcategoryTotalDTO> result = reportRepository.getTotalsBySubcategory(
                TransactionType.EXPENSE,
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31),
                "cAt",
                false,
                NO_CATEGORY_FILTER,
                false,
                BigDecimal.ZERO,
                false,
                BigDecimal.ZERO,
                false,
                BigDecimal.ZERO,
                false,
                BigDecimal.ZERO);

        assertEquals(2, result.size());

        assertEquals(
                vetCats.getId(),
                result.get(0).subcategoryId());

        assertEquals(
                vet.getId(),
                result.get(0).categoryId());

        assertEquals(
                0,
                new BigDecimal("90.00")
                        .compareTo(result.get(0).total()));

        assertEquals(
                groceriesCats.getId(),
                result.get(1).subcategoryId());

        assertEquals(
                groceries.getId(),
                result.get(1).categoryId());

        assertEquals(
                0,
                new BigDecimal("60.00")
                        .compareTo(result.get(1).total()));
    }

    @Test
    void shouldFilterAllReportAggregatesByMultipleCategories() {
        CategoryEntity groceries = createCategory("Groceries");
        CategoryEntity vet = createCategory("Veterinarian Bills");
        CategoryEntity transport = createCategory("Transport");

        SubcategoryEntity groceriesCats = createSubcategory(
                groceries,
                "Cats");

        SubcategoryEntity vetCats = createSubcategory(
                vet,
                "Cats");

        SubcategoryEntity fuel = createSubcategory(
                transport,
                "Fuel");

        TransactionEntity groceriesTransaction = createTransaction(
                groceries,
                new BigDecimal("100.00"),
                TransactionType.EXPENSE,
                LocalDate.of(2026, 10, 5));

        TransactionEntity vetTransaction = createTransaction(
                vet,
                new BigDecimal("90.00"),
                TransactionType.EXPENSE,
                LocalDate.of(2026, 10, 10));

        TransactionEntity transportTransaction = createTransaction(
                transport,
                new BigDecimal("30.00"),
                TransactionType.EXPENSE,
                LocalDate.of(2026, 10, 15));

        createSplit(
                groceriesTransaction,
                groceriesCats,
                new BigDecimal("60.00"));

        createSplit(
                vetTransaction,
                vetCats,
                new BigDecimal("90.00"));

        createSplit(
                transportTransaction,
                fuel,
                new BigDecimal("30.00"));

        LocalDate from = LocalDate.of(2026, 10, 1);
        LocalDate to = LocalDate.of(2026, 10, 31);

        List<UUID> categoryIds = List.of(
                groceries.getId(),
                vet.getId());

        // Period totals

        BigDecimal total = reportRepository.sumAmountByTypeAndPeriod(
                TransactionType.EXPENSE,
                from,
                to,
                true,
                categoryIds,
                false,
                BigDecimal.ZERO,
                false,
                BigDecimal.ZERO);

        assertEquals(
                0,
                new BigDecimal("190.00").compareTo(total));

        List<CategoryTotalDTO> categories = reportRepository.getTotalsByCategory(
                TransactionType.EXPENSE,
                from,
                to,
                true,
                categoryIds,
                false,
                BigDecimal.ZERO,
                false,
                BigDecimal.ZERO);

        assertEquals(2, categories.size());

        assertEquals(groceries.getId(), categories.get(0).categoryId());
        assertEquals(
                0,
                new BigDecimal("100.00")
                        .compareTo(categories.get(0).total()));

        assertEquals(vet.getId(), categories.get(1).categoryId());
        assertEquals(
                0,
                new BigDecimal("90.00")
                        .compareTo(categories.get(1).total()));

        List<SubcategoryTotalDTO> subcategories = reportRepository.getTotalsBySubcategory(
                TransactionType.EXPENSE,
                from,
                to,
                "cat",
                true,
                categoryIds,
                false,
                BigDecimal.ZERO,
                false,
                BigDecimal.ZERO,
                false,
                BigDecimal.ZERO,
                false,
                BigDecimal.ZERO);

        assertEquals(2, subcategories.size());

        assertEquals(
                vetCats.getId(),
                subcategories.get(0).subcategoryId());

        assertEquals(
                vet.getId(),
                subcategories.get(0).categoryId());

        assertEquals(
                0,
                new BigDecimal("90.00")
                        .compareTo(subcategories.get(0).total()));

        assertEquals(
                groceriesCats.getId(),
                subcategories.get(1).subcategoryId());

        assertEquals(
                groceries.getId(),
                subcategories.get(1).categoryId());

        assertEquals(
                0,
                new BigDecimal("60.00")
                        .compareTo(subcategories.get(1).total()));
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

    @Test
    void shouldFilterReportsByTransactionAmountRange() {
        CategoryEntity groceries = createCategory("Groceries");

        SubcategoryEntity cats = createSubcategory(
                groceries,
                "Cats");

        TransactionEntity small = createTransaction(
                groceries,
                new BigDecimal("30.00"),
                TransactionType.EXPENSE,
                LocalDate.of(2026, 10, 5));

        TransactionEntity lowerBound = createTransaction(
                groceries,
                new BigDecimal("90.00"),
                TransactionType.EXPENSE,
                LocalDate.of(2026, 10, 10));

        TransactionEntity upperBound = createTransaction(
                groceries,
                new BigDecimal("100.00"),
                TransactionType.EXPENSE,
                LocalDate.of(2026, 10, 15));

        TransactionEntity large = createTransaction(
                groceries,
                new BigDecimal("250.00"),
                TransactionType.EXPENSE,
                LocalDate.of(2026, 10, 20));

        createSplit(small, cats, new BigDecimal("10.00"));
        createSplit(lowerBound, cats, new BigDecimal("50.00"));
        createSplit(upperBound, cats, new BigDecimal("60.00"));
        createSplit(large, cats, new BigDecimal("70.00"));

        LocalDate from = LocalDate.of(2026, 10, 1);
        LocalDate to = LocalDate.of(2026, 10, 31);

        BigDecimal min = new BigDecimal("90.00");
        BigDecimal max = new BigDecimal("100.00");

        BigDecimal periodTotal = reportRepository.sumAmountByTypeAndPeriod(
                TransactionType.EXPENSE,
                from,
                to,
                false,
                NO_CATEGORY_FILTER,
                true,
                min,
                true,
                max);

        assertEquals(
                0,
                new BigDecimal("190.00").compareTo(periodTotal));

        List<CategoryTotalDTO> categories = reportRepository.getTotalsByCategory(
                TransactionType.EXPENSE,
                from,
                to,
                false,
                NO_CATEGORY_FILTER,
                true,
                min,
                true,
                max);

        assertEquals(1, categories.size());
        assertEquals(groceries.getId(), categories.get(0).categoryId());

        assertEquals(
                0,
                new BigDecimal("190.00")
                        .compareTo(categories.get(0).total()));

        List<SubcategoryTotalDTO> subcategories = reportRepository.getTotalsBySubcategory(
                TransactionType.EXPENSE,
                from,
                to,
                "cat",
                false,
                NO_CATEGORY_FILTER,
                true,
                min,
                true,
                max,
                false,
                BigDecimal.ZERO,
                false,
                BigDecimal.ZERO);

        assertEquals(1, subcategories.size());
        assertEquals(cats.getId(), subcategories.get(0).subcategoryId());

        assertEquals(
                0,
                new BigDecimal("110.00")
                        .compareTo(subcategories.get(0).total()));

        BigDecimal minOnly = reportRepository.sumAmountByTypeAndPeriod(
                TransactionType.EXPENSE,
                from,
                to,
                false,
                NO_CATEGORY_FILTER,
                true,
                new BigDecimal("100.00"),
                false,
                BigDecimal.ZERO);

        assertEquals(
                0,
                new BigDecimal("350.00").compareTo(minOnly));

        BigDecimal maxOnly = reportRepository.sumAmountByTypeAndPeriod(
                TransactionType.EXPENSE,
                from,
                to,
                false,
                NO_CATEGORY_FILTER,
                false,
                BigDecimal.ZERO,
                true,
                new BigDecimal("90.00"));

        assertEquals(
                0,
                new BigDecimal("120.00").compareTo(maxOnly));
    }

    @Test
    void shouldAggregateOnlyMatchingSplitsAcrossCategories() {
        CategoryEntity groceries = createCategory("Groceries");
        CategoryEntity vet = createCategory("Veterinarian Bills");
        CategoryEntity transport = createCategory("Transport");

        SubcategoryEntity groceriesCats = createSubcategory(groceries, "Cats");

        SubcategoryEntity groceriesFood = createSubcategory(groceries, "Food");

        SubcategoryEntity vetCats = createSubcategory(vet, "Cats");

        SubcategoryEntity checkup = createSubcategory(vet, "Checkup");

        LocalDate from = LocalDate.of(2026, 10, 1);
        LocalDate to = LocalDate.of(2026, 10, 31);

        TransactionEntity groceriesTransaction = createTransaction(
                groceries,
                new BigDecimal("100.00"),
                TransactionType.EXPENSE,
                LocalDate.of(2026, 10, 5));

        TransactionEntity vetTransaction = createTransaction(
                vet,
                new BigDecimal("200.00"),
                TransactionType.EXPENSE,
                LocalDate.of(2026, 10, 10));

        createTransaction(
                transport,
                new BigDecimal("50.00"),
                TransactionType.EXPENSE,
                LocalDate.of(2026, 10, 15));

        createSplit(
                groceriesTransaction,
                groceriesCats,
                new BigDecimal("60.00"));

        createSplit(
                groceriesTransaction,
                groceriesFood,
                new BigDecimal("30.00"));

        createSplit(
                vetTransaction,
                vetCats,
                new BigDecimal("90.00"));

        createSplit(
                vetTransaction,
                checkup,
                new BigDecimal("40.00"));

        BigDecimal minSplit = new BigDecimal("50.00");
        BigDecimal maxSplit = new BigDecimal("100.00");

        BigDecimal fullTotal = reportRepository.sumAmountByTypeAndPeriod(
                TransactionType.EXPENSE,
                from,
                to,
                false,
                NO_CATEGORY_FILTER,
                false,
                BigDecimal.ZERO,
                false,
                BigDecimal.ZERO);

        assertEquals(
                0,
                new BigDecimal("350.00").compareTo(fullTotal));

        BigDecimal filteredTotal = reportRepository.sumFilteredSplitAmountByTypeAndPeriod(
                TransactionType.EXPENSE,
                from,
                to,
                "cAt",
                false,
                NO_CATEGORY_FILTER,
                false,
                BigDecimal.ZERO,
                false,
                BigDecimal.ZERO,
                true,
                minSplit,
                true,
                maxSplit);

        assertEquals(
                0,
                new BigDecimal("150.00").compareTo(filteredTotal));

        List<CategoryTotalDTO> categories = reportRepository.getFilteredSplitTotalsByCategory(
                TransactionType.EXPENSE,
                from,
                to,
                "cAt",
                false,
                NO_CATEGORY_FILTER,
                false,
                BigDecimal.ZERO,
                false,
                BigDecimal.ZERO,
                true,
                minSplit,
                true,
                maxSplit);

        assertEquals(2, categories.size());

        assertEquals(
                vet.getId(),
                categories.get(0).categoryId());

        assertEquals(
                0,
                new BigDecimal("90.00")
                        .compareTo(categories.get(0).total()));

        assertEquals(
                groceries.getId(),
                categories.get(1).categoryId());

        assertEquals(
                0,
                new BigDecimal("60.00")
                        .compareTo(categories.get(1).total()));

        List<SubcategoryTotalDTO> subcategories = reportRepository.getTotalsBySubcategory(
                TransactionType.EXPENSE,
                from,
                to,
                "cAt",
                false,
                NO_CATEGORY_FILTER,
                false,
                BigDecimal.ZERO,
                false,
                BigDecimal.ZERO,
                true,
                minSplit,
                true,
                maxSplit);

        assertEquals(2, subcategories.size());

        assertEquals(
                vetCats.getId(),
                subcategories.get(0).subcategoryId());

        assertEquals(
                vet.getId(),
                subcategories.get(0).categoryId());

        assertEquals(
                0,
                new BigDecimal("90.00")
                        .compareTo(subcategories.get(0).total()));

        assertEquals(
                groceriesCats.getId(),
                subcategories.get(1).subcategoryId());

        assertEquals(
                groceries.getId(),
                subcategories.get(1).categoryId());

        assertEquals(
                0,
                new BigDecimal("60.00")
                        .compareTo(subcategories.get(1).total()));

        BigDecimal withoutNameFilter = reportRepository.sumFilteredSplitAmountByTypeAndPeriod(
                TransactionType.EXPENSE,
                from,
                to,
                "",
                false,
                NO_CATEGORY_FILTER,
                false,
                BigDecimal.ZERO,
                false,
                BigDecimal.ZERO,
                true,
                new BigDecimal("25.00"),
                true,
                maxSplit);

        assertEquals(
                0,
                new BigDecimal("220.00")
                        .compareTo(withoutNameFilter));

        BigDecimal combinedTotal = reportRepository.sumFilteredSplitAmountByTypeAndPeriod(
                TransactionType.EXPENSE,
                from,
                to,
                "cat",
                false,
                NO_CATEGORY_FILTER,
                false,
                BigDecimal.ZERO,
                true,
                new BigDecimal("150.00"),
                true,
                minSplit,
                true,
                maxSplit);

        assertEquals(
                0,
                new BigDecimal("60.00")
                        .compareTo(combinedTotal));

        BigDecimal noMatches = reportRepository.sumFilteredSplitAmountByTypeAndPeriod(
                TransactionType.EXPENSE,
                from,
                to,
                "cat",
                false,
                NO_CATEGORY_FILTER,
                false,
                BigDecimal.ZERO,
                false,
                BigDecimal.ZERO,
                true,
                new BigDecimal("300.00"),
                false,
                BigDecimal.ZERO);

        assertEquals(
                0,
                BigDecimal.ZERO.compareTo(noMatches));

        BigDecimal groceriesOnly = reportRepository.sumFilteredSplitAmountByTypeAndPeriod(
                TransactionType.EXPENSE,
                from,
                to,
                "cat",
                true,
                List.of(groceries.getId()),
                false,
                BigDecimal.ZERO,
                false,
                BigDecimal.ZERO,
                true,
                minSplit,
                true,
                maxSplit);

        assertEquals(
                0,
                new BigDecimal("60.00").compareTo(groceriesOnly));

        List<CategoryTotalDTO> emptyCategories = reportRepository.getFilteredSplitTotalsByCategory(
                TransactionType.EXPENSE,
                from, to, "cat",
                false, NO_CATEGORY_FILTER,
                false, BigDecimal.ZERO,
                false, BigDecimal.ZERO,
                true, new BigDecimal("300.00"),
                false, BigDecimal.ZERO);

        List<SubcategoryTotalDTO> emptySubcategories = reportRepository.getTotalsBySubcategory(
                TransactionType.EXPENSE,
                from, to, "cat",
                false, NO_CATEGORY_FILTER,
                false, BigDecimal.ZERO,
                false, BigDecimal.ZERO,
                true, new BigDecimal("300.00"),
                false, BigDecimal.ZERO);

        assertEquals(0, emptyCategories.size());
        assertEquals(0, emptySubcategories.size());
    }
}