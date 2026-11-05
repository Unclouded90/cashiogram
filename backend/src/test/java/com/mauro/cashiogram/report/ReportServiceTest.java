package com.mauro.cashiogram.report;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mauro.cashiogram.exception.BadRequestException;
import com.mauro.cashiogram.report.dto.CategoryTotalDTO;
import com.mauro.cashiogram.report.dto.PeriodTotalDTO;
import com.mauro.cashiogram.report.dto.SubcategoryTotalDTO;
import com.mauro.cashiogram.report.repository.ReportRepository;
import com.mauro.cashiogram.transaction.entity.TransactionType;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;

    private ReportService reportService;

    private static final List<UUID> NO_CATEGORY_FILTER = List.of(new UUID(0L, 0L));

    @BeforeEach
    void setUp() {
        reportService = new ReportService(reportRepository);
    }

    @Test
    void shouldGetPeriodTotal() {
        LocalDate from = LocalDate.of(2026, 10, 1);
        LocalDate to = LocalDate.of(2026, 10, 31);

        when(reportRepository.sumAmountByTypeAndPeriod(
                TransactionType.INCOME,
                from,
                to,
                false,
                NO_CATEGORY_FILTER,
                false,
                BigDecimal.ZERO,
                false,
                BigDecimal.ZERO))
                .thenReturn(new BigDecimal("2200.00"));

        when(reportRepository.sumAmountByTypeAndPeriod(
                TransactionType.EXPENSE,
                from,
                to,
                false,
                NO_CATEGORY_FILTER,
                false,
                BigDecimal.ZERO,
                false,
                BigDecimal.ZERO))
                .thenReturn(new BigDecimal("850.00"));

        PeriodTotalDTO result = reportService.getPeriodTotal(from, to, null, null, null, null, null, null);

        assertEquals(from, result.from());
        assertEquals(to, result.to());

        assertEquals(
                new BigDecimal("2200.00"),
                result.totalIncome());

        assertEquals(
                new BigDecimal("850.00"),
                result.totalExpenses());

        assertEquals(
                new BigDecimal("1350.00"),
                result.netTotal());
    }

    @Test
    void shouldGetCategoryTotals() {
        LocalDate from = LocalDate.of(2026, 10, 1);
        LocalDate to = LocalDate.of(2026, 10, 31);

        CategoryTotalDTO groceries = new CategoryTotalDTO(
                UUID.randomUUID(),
                "Groceries",
                new BigDecimal("420.00"));

        CategoryTotalDTO bills = new CategoryTotalDTO(
                UUID.randomUUID(),
                "Bills",
                new BigDecimal("180.00"));

        when(reportRepository.getTotalsByCategory(
                TransactionType.EXPENSE,
                from,
                to,
                false,
                NO_CATEGORY_FILTER,
                false,
                BigDecimal.ZERO,
                false,
                BigDecimal.ZERO))
                .thenReturn(List.of(groceries, bills));

        List<CategoryTotalDTO> result = reportService.getCategoryTotals(
                TransactionType.EXPENSE,
                from,
                to,
                null,
                null,
                null,
                null,
                null,
                null);

        assertEquals(2, result.size());

        assertEquals(
                "Groceries",
                result.get(0).categoryName());

        assertEquals(
                new BigDecimal("420.00"),
                result.get(0).total());

        verify(reportRepository)
                .getTotalsByCategory(
                        TransactionType.EXPENSE,
                        from,
                        to,
                        false,
                        NO_CATEGORY_FILTER,
                        false,
                        BigDecimal.ZERO,
                        false,
                        BigDecimal.ZERO);
    }

    @Test
    void shouldGetSubcategoryTotals() {
        LocalDate from = LocalDate.of(2026, 10, 1);
        LocalDate to = LocalDate.of(2026, 10, 31);

        UUID categoryId = UUID.randomUUID();

        SubcategoryTotalDTO food = new SubcategoryTotalDTO(
                UUID.randomUUID(),
                "Food",
                categoryId,
                "Groceries",
                new BigDecimal("250.00"));

        SubcategoryTotalDTO cats = new SubcategoryTotalDTO(
                UUID.randomUUID(),
                "Cat Products",
                categoryId,
                "Groceries",
                new BigDecimal("90.00"));

        when(reportRepository.getTotalsBySubcategory(
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
                false,
                BigDecimal.ZERO,
                false,
                BigDecimal.ZERO))
                .thenReturn(List.of(food, cats));

        List<SubcategoryTotalDTO> result = reportService.getSubcategoryTotals(
                TransactionType.EXPENSE,
                from,
                to,
                null,
                null,
                null,
                null,
                null,
                null);

        assertEquals(2, result.size());

        assertEquals(
                "Food",
                result.get(0).subcategoryName());

        assertEquals(
                new BigDecimal("250.00"),
                result.get(0).total());

        verify(reportRepository)
                .getTotalsBySubcategory(
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
                        false,
                        BigDecimal.ZERO,
                        false,
                        BigDecimal.ZERO);
    }

    @Test
    void shouldNormalizeSubcategoryName() {
        LocalDate from = LocalDate.of(2026, 10, 1);
        LocalDate to = LocalDate.of(2026, 10, 31);

        when(reportRepository.getTotalsBySubcategory(
                TransactionType.EXPENSE,
                from,
                to,
                "Cat",
                false,
                NO_CATEGORY_FILTER,
                false,
                BigDecimal.ZERO,
                false,
                BigDecimal.ZERO,
                false,
                BigDecimal.ZERO,
                false,
                BigDecimal.ZERO))
                .thenReturn(List.of());

        List<SubcategoryTotalDTO> result = reportService.getSubcategoryTotals(
                TransactionType.EXPENSE,
                from,
                to,
                "  Cat  ",
                null,
                null,
                null,
                null,
                null);

        assertEquals(0, result.size());

        verify(reportRepository)
                .getTotalsBySubcategory(
                        TransactionType.EXPENSE,
                        from,
                        to,
                        "Cat",
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
    }

    @Test
    void shouldFailWhenFromDateIsAfterToDate() {
        LocalDate from = LocalDate.of(2026, 10, 31);
        LocalDate to = LocalDate.of(2026, 10, 1);

        assertThrows(
                BadRequestException.class,
                () -> reportService.getPeriodTotal(from, to, null, null, null, null, null, null));

        verify(reportRepository, never())
                .sumAmountByTypeAndPeriod(
                        TransactionType.INCOME,
                        from,
                        to,
                        false,
                        NO_CATEGORY_FILTER,
                        false,
                        BigDecimal.ZERO,
                        false,
                        BigDecimal.ZERO);

        verify(reportRepository, never())
                .sumAmountByTypeAndPeriod(
                        TransactionType.EXPENSE,
                        from,
                        to,
                        false,
                        NO_CATEGORY_FILTER,
                        false,
                        BigDecimal.ZERO,
                        false,
                        BigDecimal.ZERO);
    }

    @Test
    void shouldFailWhenPeriodDateIsMissing() {
        LocalDate to = LocalDate.of(2026, 10, 31);

        assertThrows(
                BadRequestException.class,
                () -> reportService.getPeriodTotal(
                        null,
                        to,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null));
    }

    @Test
    void shouldApplyCategoryFilterToAllReports() {
        LocalDate from = LocalDate.of(2026, 10, 1);
        LocalDate to = LocalDate.of(2026, 10, 31);

        UUID groceriesId = UUID.randomUUID();
        UUID vetId = UUID.randomUUID();

        List<UUID> categoryIds = List.of(groceriesId, vetId);

        CategoryTotalDTO groceries = new CategoryTotalDTO(
                groceriesId,
                "Groceries",
                new BigDecimal("100.00"));

        SubcategoryTotalDTO cats = new SubcategoryTotalDTO(
                UUID.randomUUID(),
                "Cats",
                groceriesId,
                "Groceries",
                new BigDecimal("60.00"));

        when(reportRepository.sumAmountByTypeAndPeriod(
                TransactionType.INCOME,
                from,
                to,
                true,
                categoryIds,
                false,
                BigDecimal.ZERO,
                false,
                BigDecimal.ZERO))
                .thenReturn(new BigDecimal("50.00"));

        when(reportRepository.sumAmountByTypeAndPeriod(
                TransactionType.EXPENSE,
                from,
                to,
                true,
                categoryIds,
                false,
                BigDecimal.ZERO,
                false,
                BigDecimal.ZERO))
                .thenReturn(new BigDecimal("100.00"));

        when(reportRepository.getTotalsByCategory(
                TransactionType.EXPENSE,
                from,
                to,
                true,
                categoryIds,
                false,
                BigDecimal.ZERO,
                false,
                BigDecimal.ZERO))
                .thenReturn(List.of(groceries));

        when(reportRepository.getTotalsBySubcategory(
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
                BigDecimal.ZERO))
                .thenReturn(List.of(cats));

        PeriodTotalDTO period = reportService.getPeriodTotal(
                from,
                to,
                categoryIds,
                null,
                null,
                null,
                null,
                null);

        List<CategoryTotalDTO> categories = reportService.getCategoryTotals(
                TransactionType.EXPENSE,
                from,
                to,
                categoryIds,
                null,
                null,
                null,
                null,
                null);

        List<SubcategoryTotalDTO> subcategories = reportService.getSubcategoryTotals(
                TransactionType.EXPENSE,
                from,
                to,
                "cat",
                categoryIds,
                null,
                null,
                null,
                null);

        assertEquals(new BigDecimal("50.00"), period.totalIncome());
        assertEquals(new BigDecimal("100.00"), period.totalExpenses());
        assertEquals(new BigDecimal("-50.00"), period.netTotal());

        assertEquals(1, categories.size());
        assertEquals(groceriesId, categories.get(0).categoryId());

        assertEquals(1, subcategories.size());
        assertEquals("Cats", subcategories.get(0).subcategoryName());

        verify(reportRepository).sumAmountByTypeAndPeriod(
                TransactionType.INCOME,
                from,
                to,
                true,
                categoryIds,
                false,
                BigDecimal.ZERO,
                false,
                BigDecimal.ZERO);

        verify(reportRepository).sumAmountByTypeAndPeriod(
                TransactionType.EXPENSE,
                from,
                to,
                true,
                categoryIds,
                false,
                BigDecimal.ZERO,
                false,
                BigDecimal.ZERO);

        verify(reportRepository).getTotalsByCategory(
                TransactionType.EXPENSE,
                from,
                to,
                true,
                categoryIds,
                false,
                BigDecimal.ZERO,
                false,
                BigDecimal.ZERO);

        verify(reportRepository).getTotalsBySubcategory(
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
    }

    @Test
    void shouldApplyTransactionAmountRange() {
        LocalDate from = LocalDate.of(2026, 10, 1);
        LocalDate to = LocalDate.of(2026, 10, 31);

        UUID categoryId = UUID.randomUUID();
        List<UUID> categoryIds = List.of(categoryId);

        BigDecimal min = new BigDecimal("50.00");
        BigDecimal max = new BigDecimal("200.00");

        when(reportRepository.sumAmountByTypeAndPeriod(
                TransactionType.INCOME,
                from,
                to,
                true,
                categoryIds,
                true,
                min,
                true,
                max))
                .thenReturn(new BigDecimal("100.00"));

        when(reportRepository.sumAmountByTypeAndPeriod(
                TransactionType.EXPENSE,
                from,
                to,
                true,
                categoryIds,
                true,
                min,
                true,
                max))
                .thenReturn(new BigDecimal("150.00"));

        PeriodTotalDTO period = reportService.getPeriodTotal(
                from, to, categoryIds, min, max, null, null, null);

        reportService.getCategoryTotals(
                TransactionType.EXPENSE,
                from, to, categoryIds, min, max, null, null, null);

        reportService.getSubcategoryTotals(
                TransactionType.EXPENSE,
                from, to, "cat", categoryIds, min, max, null, null);

        assertEquals(new BigDecimal("100.00"), period.totalIncome());
        assertEquals(new BigDecimal("150.00"), period.totalExpenses());
        assertEquals(new BigDecimal("-50.00"), period.netTotal());

        verify(reportRepository).getTotalsByCategory(
                TransactionType.EXPENSE,
                from,
                to,
                true,
                categoryIds,
                true,
                min,
                true,
                max);

        verify(reportRepository).getTotalsBySubcategory(
                TransactionType.EXPENSE,
                from,
                to,
                "cat",
                true,
                categoryIds,
                true,
                min,
                true,
                max,
                false,
                BigDecimal.ZERO,
                false,
                BigDecimal.ZERO);
    }

    @Test
    void shouldRejectInvalidAmountRange() {
        LocalDate from = LocalDate.of(2026, 10, 1);
        LocalDate to = LocalDate.of(2026, 10, 31);

        assertThrows(
                BadRequestException.class,
                () -> reportService.getPeriodTotal(
                        from, to, null,
                        new BigDecimal("-1.00"), null,
                        null, null, null));

        assertThrows(
                BadRequestException.class,
                () -> reportService.getCategoryTotals(
                        TransactionType.EXPENSE,
                        from, to, null,
                        null, new BigDecimal("-5.00"),
                        null, null, null));

        assertThrows(
                BadRequestException.class,
                () -> reportService.getSubcategoryTotals(
                        TransactionType.EXPENSE,
                        from, to, null, null,
                        new BigDecimal("200.00"),
                        new BigDecimal("100.00"),
                        null, null));

        verifyNoInteractions(reportRepository);
    }

    @Test
    void shouldApplySubcategoryAndSplitAmountFilters() {
        LocalDate from = LocalDate.of(2026, 10, 1);
        LocalDate to = LocalDate.of(2026, 10, 31);

        UUID categoryId = UUID.randomUUID();
        UUID subcategoryId = UUID.randomUUID();

        List<UUID> categoryIds = List.of(categoryId);

        BigDecimal minAmount = new BigDecimal("50.00");
        BigDecimal maxAmount = new BigDecimal("200.00");

        BigDecimal minSplit = new BigDecimal("20.00");
        BigDecimal maxSplit = new BigDecimal("100.00");

        CategoryTotalDTO category = new CategoryTotalDTO(
                categoryId,
                "Groceries",
                new BigDecimal("60.00"));

        SubcategoryTotalDTO subcategory = new SubcategoryTotalDTO(
                subcategoryId,
                "Cats",
                categoryId,
                "Groceries",
                new BigDecimal("60.00"));

        when(reportRepository.sumFilteredSplitAmountByTypeAndPeriod(
                TransactionType.INCOME,
                from, to, "cat",
                true, categoryIds,
                true, minAmount,
                true, maxAmount,
                true, minSplit,
                true, maxSplit))
                .thenReturn(BigDecimal.ZERO);

        when(reportRepository.sumFilteredSplitAmountByTypeAndPeriod(
                TransactionType.EXPENSE,
                from, to, "cat",
                true, categoryIds,
                true, minAmount,
                true, maxAmount,
                true, minSplit,
                true, maxSplit))
                .thenReturn(new BigDecimal("60.00"));

        when(reportRepository.getFilteredSplitTotalsByCategory(
                TransactionType.EXPENSE,
                from, to, "cat",
                true, categoryIds,
                true, minAmount,
                true, maxAmount,
                true, minSplit,
                true, maxSplit))
                .thenReturn(List.of(category));

        when(reportRepository.getTotalsBySubcategory(
                TransactionType.EXPENSE,
                from, to, "cat",
                true, categoryIds,
                true, minAmount,
                true, maxAmount,
                true, minSplit,
                true, maxSplit))
                .thenReturn(List.of(subcategory));

        PeriodTotalDTO period = reportService.getPeriodTotal(
                from, to, categoryIds,
                minAmount, maxAmount,
                "  cat  ", minSplit, maxSplit);

        List<CategoryTotalDTO> categories = reportService.getCategoryTotals(
                TransactionType.EXPENSE,
                from, to, categoryIds,
                minAmount, maxAmount,
                "  cat  ", minSplit, maxSplit);

        List<SubcategoryTotalDTO> subcategories = reportService.getSubcategoryTotals(
                TransactionType.EXPENSE,
                from, to, "  cat  ", categoryIds,
                minAmount, maxAmount,
                minSplit, maxSplit);

        assertEquals(BigDecimal.ZERO, period.totalIncome());
        assertEquals(
                new BigDecimal("60.00"),
                period.totalExpenses());

        assertEquals(
                new BigDecimal("-60.00"),
                period.netTotal());

        assertEquals(1, categories.size());
        assertEquals(categoryId, categories.get(0).categoryId());

        assertEquals(1, subcategories.size());
        assertEquals(subcategoryId, subcategories.get(0).subcategoryId());

        verify(reportRepository).sumFilteredSplitAmountByTypeAndPeriod(
                TransactionType.EXPENSE,
                from, to, "cat",
                true, categoryIds,
                true, minAmount,
                true, maxAmount,
                true, minSplit,
                true, maxSplit);

        verify(reportRepository).getFilteredSplitTotalsByCategory(
                TransactionType.EXPENSE,
                from, to, "cat",
                true, categoryIds,
                true, minAmount,
                true, maxAmount,
                true, minSplit,
                true, maxSplit);

        verify(reportRepository).getTotalsBySubcategory(
                TransactionType.EXPENSE,
                from, to, "cat",
                true, categoryIds,
                true, minAmount,
                true, maxAmount,
                true, minSplit,
                true, maxSplit);
    }

    @Test
    void shouldUseSplitsWhenFilteringBySubcategoryNameOnly() {
        LocalDate from = LocalDate.of(2026, 10, 1);
        LocalDate to = LocalDate.of(2026, 10, 31);

        when(reportRepository.sumFilteredSplitAmountByTypeAndPeriod(
                TransactionType.INCOME,
                from, to, "cat",
                false, NO_CATEGORY_FILTER,
                false, BigDecimal.ZERO,
                false, BigDecimal.ZERO,
                false, BigDecimal.ZERO,
                false, BigDecimal.ZERO))
                .thenReturn(BigDecimal.ZERO);

        when(reportRepository.sumFilteredSplitAmountByTypeAndPeriod(
                TransactionType.EXPENSE,
                from, to, "cat",
                false, NO_CATEGORY_FILTER,
                false, BigDecimal.ZERO,
                false, BigDecimal.ZERO,
                false, BigDecimal.ZERO,
                false, BigDecimal.ZERO))
                .thenReturn(new BigDecimal("150.00"));

        PeriodTotalDTO result = reportService.getPeriodTotal(
                from, to, null, null, null, "cat", null, null);

        assertEquals(
                new BigDecimal("150.00"),
                result.totalExpenses());

        verify(reportRepository).sumFilteredSplitAmountByTypeAndPeriod(
                TransactionType.EXPENSE,
                from, to, "cat",
                false, NO_CATEGORY_FILTER,
                false, BigDecimal.ZERO,
                false, BigDecimal.ZERO,
                false, BigDecimal.ZERO,
                false, BigDecimal.ZERO);
    }

    @Test
    void shouldUseSplitsWhenFilteringBySplitAmountOnly() {
        LocalDate from = LocalDate.of(2026, 10, 1);
        LocalDate to = LocalDate.of(2026, 10, 31);

        BigDecimal minSplit = new BigDecimal("50.00");

        reportService.getCategoryTotals(
                TransactionType.EXPENSE,
                from, to, null,
                null, null, null,
                minSplit, null);

        verify(reportRepository).getFilteredSplitTotalsByCategory(
                TransactionType.EXPENSE,
                from, to, "",
                false, NO_CATEGORY_FILTER,
                false, BigDecimal.ZERO,
                false, BigDecimal.ZERO,
                true, minSplit,
                false, BigDecimal.ZERO);
    }

    @Test
    void shouldRejectInvalidSplitAmountRange() {
        LocalDate from = LocalDate.of(2026, 10, 1);
        LocalDate to = LocalDate.of(2026, 10, 31);

        assertThrows(
                BadRequestException.class,
                () -> reportService.getPeriodTotal(
                        from, to, null, null, null,
                        null, new BigDecimal("-1.00"), null));

        assertThrows(
                BadRequestException.class,
                () -> reportService.getCategoryTotals(
                        TransactionType.EXPENSE,
                        from, to, null, null, null,
                        null, null, new BigDecimal("-5.00")));

        assertThrows(
                BadRequestException.class,
                () -> reportService.getSubcategoryTotals(
                        TransactionType.EXPENSE,
                        from, to, null, null, null, null,
                        new BigDecimal("100.00"),
                        new BigDecimal("50.00")));

        verifyNoInteractions(reportRepository);
    }
}