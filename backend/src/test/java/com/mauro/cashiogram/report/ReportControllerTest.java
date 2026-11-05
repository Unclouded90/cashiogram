package com.mauro.cashiogram.report;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.mauro.cashiogram.exception.BadRequestException;
import com.mauro.cashiogram.report.dto.CategoryTotalDTO;
import com.mauro.cashiogram.report.dto.PeriodTotalDTO;
import com.mauro.cashiogram.report.dto.SubcategoryTotalDTO;
import com.mauro.cashiogram.transaction.entity.TransactionType;

@WebMvcTest(ReportController.class)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReportService reportService;

    @Test
    void shouldGetPeriodTotal() throws Exception {
        LocalDate from = LocalDate.of(2026, 10, 1);
        LocalDate to = LocalDate.of(2026, 10, 31);

        PeriodTotalDTO response = new PeriodTotalDTO(
                from,
                to,
                new BigDecimal("2200.00"),
                new BigDecimal("850.00"),
                new BigDecimal("1350.00"));

        when(reportService.getPeriodTotal(from, to, null, null, null, null, null, null))
                .thenReturn(response);

        mockMvc.perform(
                get("/reports/period")
                        .param("from", "2026-10-01")
                        .param("to", "2026-10-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.from")
                        .value("2026-10-01"))
                .andExpect(jsonPath("$.to")
                        .value("2026-10-31"))
                .andExpect(jsonPath("$.totalIncome")
                        .value(2200.00))
                .andExpect(jsonPath("$.totalExpenses")
                        .value(850.00))
                .andExpect(jsonPath("$.netTotal")
                        .value(1350.00));

        verify(reportService)
                .getPeriodTotal(from, to, null, null, null, null, null, null);
    }

    @Test
    void shouldGetCategoryTotals() throws Exception {
        LocalDate from = LocalDate.of(2026, 10, 1);
        LocalDate to = LocalDate.of(2026, 10, 31);

        UUID categoryId = UUID.randomUUID();

        CategoryTotalDTO category = new CategoryTotalDTO(
                categoryId,
                "Groceries",
                new BigDecimal("420.00"));

        when(reportService.getCategoryTotals(
                TransactionType.EXPENSE,
                from,
                to,
                null,
                null,
                null,
                null,
                null,
                null))
                .thenReturn(List.of(category));

        mockMvc.perform(
                get("/reports/categories")
                        .param("type", "EXPENSE")
                        .param("from", "2026-10-01")
                        .param("to", "2026-10-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].categoryId")
                        .value(categoryId.toString()))
                .andExpect(jsonPath("$[0].categoryName")
                        .value("Groceries"))
                .andExpect(jsonPath("$[0].total")
                        .value(420.00));

        verify(reportService)
                .getCategoryTotals(
                        TransactionType.EXPENSE,
                        from,
                        to,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null);
    }

    @Test
    void shouldGetSubcategoryTotals() throws Exception {
        LocalDate from = LocalDate.of(2026, 10, 1);
        LocalDate to = LocalDate.of(2026, 10, 31);

        UUID subcategoryId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        SubcategoryTotalDTO subcategory = new SubcategoryTotalDTO(
                subcategoryId,
                "Food",
                categoryId,
                "Groceries",
                new BigDecimal("250.00"));

        when(reportService.getSubcategoryTotals(
                TransactionType.EXPENSE,
                from,
                to,
                null,
                null,
                null,
                null,
                null,
                null))
                .thenReturn(List.of(subcategory));

        mockMvc.perform(
                get("/reports/subcategories")
                        .param("type", "EXPENSE")
                        .param("from", "2026-10-01")
                        .param("to", "2026-10-31"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].subcategoryId")
                        .value(subcategoryId.toString()))
                .andExpect(jsonPath("$[0].subcategoryName")
                        .value("Food"))
                .andExpect(jsonPath("$[0].categoryId")
                        .value(categoryId.toString()))
                .andExpect(jsonPath("$[0].categoryName")
                        .value("Groceries"))
                .andExpect(jsonPath("$[0].total")
                        .value(250.00));

        verify(reportService)
                .getSubcategoryTotals(
                        TransactionType.EXPENSE,
                        from,
                        to,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null);
    }

    @Test
    void shouldGetSubcategoryTotalsByName() throws Exception {
        LocalDate from = LocalDate.of(2026, 10, 1);
        LocalDate to = LocalDate.of(2026, 10, 31);

        UUID subcategoryId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();

        SubcategoryTotalDTO cats = new SubcategoryTotalDTO(
                subcategoryId,
                "Cats",
                categoryId,
                "Groceries",
                new BigDecimal("60.00"));

        when(reportService.getSubcategoryTotals(
                TransactionType.EXPENSE,
                from,
                to,
                "cat",
                null,
                null,
                null,
                null,
                null))
                .thenReturn(List.of(cats));

        mockMvc.perform(
                get("/reports/subcategories")
                        .param("type", "EXPENSE")
                        .param("from", "2026-10-01")
                        .param("to", "2026-10-31")
                        .param("subcategoryName", "cat"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].subcategoryName")
                        .value("Cats"))
                .andExpect(jsonPath("$[0].total")
                        .value(60.00));

        verify(reportService)
                .getSubcategoryTotals(
                        TransactionType.EXPENSE,
                        from,
                        to,
                        "cat",
                        null,
                        null,
                        null,
                        null,
                        null);
    }

    @Test
    void shouldReturnBadRequestWhenPeriodIsInvalid()
            throws Exception {

        LocalDate from = LocalDate.of(2026, 10, 31);
        LocalDate to = LocalDate.of(2026, 10, 1);

        when(reportService.getPeriodTotal(from, to, null, null, null, null, null, null))
                .thenThrow(
                        new BadRequestException(
                                "From date cannot be after to date"));

        mockMvc.perform(
                get("/reports/period")
                        .param("from", "2026-10-31")
                        .param("to", "2026-10-01"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status")
                        .value(400))
                .andExpect(jsonPath("$.message")
                        .value(
                                "From date cannot be after to date"));
    }

    @Test
    void shouldReturnBadRequestWhenRequiredDateIsMissing()
            throws Exception {

        mockMvc.perform(
                get("/reports/period")
                        .param("from", "2026-10-01"))
                .andExpect(status().isBadRequest());

        verify(reportService, never())
                .getPeriodTotal(
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any(),
                        org.mockito.ArgumentMatchers.any());
    }

    @Test
    void shouldForwardMultipleCategoryIdsToAllReports() throws Exception {
        LocalDate from = LocalDate.of(2026, 10, 1);
        LocalDate to = LocalDate.of(2026, 10, 31);

        UUID groceriesId = UUID.randomUUID();
        UUID vetId = UUID.randomUUID();

        List<UUID> categoryIds = List.of(groceriesId, vetId);

        PeriodTotalDTO period = new PeriodTotalDTO(
                from,
                to,
                new BigDecimal("0.00"),
                new BigDecimal("190.00"),
                new BigDecimal("-190.00"));

        when(reportService.getPeriodTotal(from, to, categoryIds, null, null, null, null, null))
                .thenReturn(period);

        when(reportService.getCategoryTotals(
                TransactionType.EXPENSE,
                from,
                to,
                categoryIds,
                null,
                null,
                null,
                null,
                null))
                .thenReturn(List.of());

        when(reportService.getSubcategoryTotals(
                TransactionType.EXPENSE,
                from,
                to,
                "cat",
                categoryIds,
                null,
                null,
                null,
                null))
                .thenReturn(List.of());

        mockMvc.perform(
                get("/reports/period")
                        .param("from", "2026-10-01")
                        .param("to", "2026-10-31")
                        .param("categoryIds",
                                groceriesId.toString(),
                                vetId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalExpenses")
                        .value(190.00));

        mockMvc.perform(
                get("/reports/categories")
                        .param("type", "EXPENSE")
                        .param("from", "2026-10-01")
                        .param("to", "2026-10-31")
                        .param("categoryIds",
                                groceriesId.toString(),
                                vetId.toString()))
                .andExpect(status().isOk());

        mockMvc.perform(
                get("/reports/subcategories")
                        .param("type", "EXPENSE")
                        .param("from", "2026-10-01")
                        .param("to", "2026-10-31")
                        .param("subcategoryName", "cat")
                        .param("categoryIds",
                                groceriesId.toString(),
                                vetId.toString()))
                .andExpect(status().isOk());

        verify(reportService).getPeriodTotal(
                from,
                to,
                categoryIds,
                null,
                null,
                null,
                null,
                null);

        verify(reportService).getCategoryTotals(
                TransactionType.EXPENSE,
                from,
                to,
                categoryIds,
                null,
                null,
                null,
                null,
                null);

        verify(reportService).getSubcategoryTotals(
                TransactionType.EXPENSE,
                from,
                to,
                "cat",
                categoryIds,
                null,
                null,
                null,
                null);
    }

    @Test
    void shouldForwardTransactionAmountFiltersToAllReports()
            throws Exception {

        LocalDate from = LocalDate.of(2026, 10, 1);
        LocalDate to = LocalDate.of(2026, 10, 31);

        BigDecimal min = new BigDecimal("50.00");
        BigDecimal max = new BigDecimal("200.00");

        PeriodTotalDTO period = new PeriodTotalDTO(
                from,
                to,
                new BigDecimal("100.00"),
                new BigDecimal("150.00"),
                new BigDecimal("-50.00"));

        when(reportService.getPeriodTotal(
                from, to, null, min, max, null, null, null))
                .thenReturn(period);

        when(reportService.getCategoryTotals(
                TransactionType.EXPENSE,
                from,
                to,
                null,
                min,
                max,
                null,
                null,
                null))
                .thenReturn(List.of());

        when(reportService.getSubcategoryTotals(
                TransactionType.EXPENSE,
                from,
                to,
                "cat",
                null,
                min,
                max,
                null,
                null))
                .thenReturn(List.of());

        mockMvc.perform(
                get("/reports/period")
                        .param("from", "2026-10-01")
                        .param("to", "2026-10-31")
                        .param("minAmount", "50.00")
                        .param("maxAmount", "200.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalExpenses").value(150.00));

        mockMvc.perform(
                get("/reports/categories")
                        .param("type", "EXPENSE")
                        .param("from", "2026-10-01")
                        .param("to", "2026-10-31")
                        .param("minAmount", "50.00")
                        .param("maxAmount", "200.00"))
                .andExpect(status().isOk());

        mockMvc.perform(
                get("/reports/subcategories")
                        .param("type", "EXPENSE")
                        .param("from", "2026-10-01")
                        .param("to", "2026-10-31")
                        .param("subcategoryName", "cat")
                        .param("minAmount", "50.00")
                        .param("maxAmount", "200.00"))
                .andExpect(status().isOk());

        verify(reportService).getPeriodTotal(
                from, to, null, min, max, null, null, null);

        verify(reportService).getCategoryTotals(
                TransactionType.EXPENSE,
                from,
                to,
                null,
                min,
                max,
                null,
                null,
                null);

        verify(reportService).getSubcategoryTotals(
                TransactionType.EXPENSE,
                from,
                to,
                "cat",
                null,
                min,
                max,
                null,
                null);
    }

    @Test
    void shouldForwardSplitFiltersToAllReports() throws Exception {
        LocalDate from = LocalDate.of(2026, 10, 1);
        LocalDate to = LocalDate.of(2026, 10, 31);

        BigDecimal minSplit = new BigDecimal("20.00");
        BigDecimal maxSplit = new BigDecimal("100.00");

        PeriodTotalDTO period = new PeriodTotalDTO(
                from,
                to,
                BigDecimal.ZERO,
                new BigDecimal("150.00"),
                new BigDecimal("-150.00"));

        when(reportService.getPeriodTotal(
                from, to, null, null, null,
                "cat", minSplit, maxSplit))
                .thenReturn(period);

        when(reportService.getCategoryTotals(
                TransactionType.EXPENSE,
                from, to, null, null, null,
                "cat", minSplit, maxSplit))
                .thenReturn(List.of());

        when(reportService.getSubcategoryTotals(
                TransactionType.EXPENSE,
                from, to, "cat", null,
                null, null, minSplit, maxSplit))
                .thenReturn(List.of());

        mockMvc.perform(
                get("/reports/period")
                        .param("from", "2026-10-01")
                        .param("to", "2026-10-31")
                        .param("subcategoryName", "cat")
                        .param("minSplitAmount", "20.00")
                        .param("maxSplitAmount", "100.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalExpenses")
                        .value(150.00));

        mockMvc.perform(
                get("/reports/categories")
                        .param("type", "EXPENSE")
                        .param("from", "2026-10-01")
                        .param("to", "2026-10-31")
                        .param("subcategoryName", "cat")
                        .param("minSplitAmount", "20.00")
                        .param("maxSplitAmount", "100.00"))
                .andExpect(status().isOk());

        mockMvc.perform(
                get("/reports/subcategories")
                        .param("type", "EXPENSE")
                        .param("from", "2026-10-01")
                        .param("to", "2026-10-31")
                        .param("subcategoryName", "cat")
                        .param("minSplitAmount", "20.00")
                        .param("maxSplitAmount", "100.00"))
                .andExpect(status().isOk());

        verify(reportService).getPeriodTotal(
                from, to, null, null, null,
                "cat", minSplit, maxSplit);

        verify(reportService).getCategoryTotals(
                TransactionType.EXPENSE,
                from, to, null, null, null,
                "cat", minSplit, maxSplit);

        verify(reportService).getSubcategoryTotals(
                TransactionType.EXPENSE,
                from, to, "cat", null,
                null, null, minSplit, maxSplit);
    }

    @Test
    void shouldReturnBadRequestWhenSplitAmountRangeIsInvalid()
            throws Exception {

        LocalDate from = LocalDate.of(2026, 10, 1);
        LocalDate to = LocalDate.of(2026, 10, 31);

        BigDecimal min = new BigDecimal("100.00");
        BigDecimal max = new BigDecimal("50.00");

        when(reportService.getPeriodTotal(
                from, to, null, null, null,
                null, min, max))
                .thenThrow(new BadRequestException(
                        "Minimum split amount cannot exceed maximum split amount"));

        mockMvc.perform(
                get("/reports/period")
                        .param("from", "2026-10-01")
                        .param("to", "2026-10-31")
                        .param("minSplitAmount", "100.00")
                        .param("maxSplitAmount", "50.00"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value(
                                "Minimum split amount cannot exceed maximum split amount"));
    }
}