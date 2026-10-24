package com.mauro.cashiogram.report;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
                to))
                .thenReturn(new BigDecimal("2200.00"));

        when(reportRepository.sumAmountByTypeAndPeriod(
                TransactionType.EXPENSE,
                from,
                to))
                .thenReturn(new BigDecimal("850.00"));

        PeriodTotalDTO result = reportService.getPeriodTotal(from, to);

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
                to))
                .thenReturn(List.of(groceries, bills));

        List<CategoryTotalDTO> result = reportService.getCategoryTotals(
                TransactionType.EXPENSE,
                from,
                to);

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
                        to);
    }

    @Test
    void shouldGetSubcategoryTotals() {
        LocalDate from = LocalDate.of(2026, 10, 1);
        LocalDate to = LocalDate.of(2026, 10, 31);

        SubcategoryTotalDTO food = new SubcategoryTotalDTO(
                UUID.randomUUID(),
                "Food",
                new BigDecimal("250.00"));

        SubcategoryTotalDTO cats = new SubcategoryTotalDTO(
                UUID.randomUUID(),
                "Cat Products",
                new BigDecimal("90.00"));

        when(reportRepository.getTotalsBySubcategory(
                TransactionType.EXPENSE,
                from,
                to))
                .thenReturn(List.of(food, cats));

        List<SubcategoryTotalDTO> result = reportService.getSubcategoryTotals(
                TransactionType.EXPENSE,
                from,
                to);

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
                        to);
    }

    @Test
    void shouldFailWhenFromDateIsAfterToDate() {
        LocalDate from = LocalDate.of(2026, 10, 31);
        LocalDate to = LocalDate.of(2026, 10, 1);

        assertThrows(
                BadRequestException.class,
                () -> reportService.getPeriodTotal(from, to));

        verify(reportRepository, never())
                .sumAmountByTypeAndPeriod(
                        TransactionType.INCOME,
                        from,
                        to);

        verify(reportRepository, never())
                .sumAmountByTypeAndPeriod(
                        TransactionType.EXPENSE,
                        from,
                        to);
    }

    @Test
    void shouldFailWhenPeriodDateIsMissing() {
        LocalDate to = LocalDate.of(2026, 10, 31);

        assertThrows(
                BadRequestException.class,
                () -> reportService.getPeriodTotal(
                        null,
                        to));
    }
}