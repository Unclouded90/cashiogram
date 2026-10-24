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

        when(reportService.getPeriodTotal(from, to))
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
                .getPeriodTotal(from, to);
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
                to))
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
                        to);
    }

    @Test
    void shouldGetSubcategoryTotals() throws Exception {
        LocalDate from = LocalDate.of(2026, 10, 1);
        LocalDate to = LocalDate.of(2026, 10, 31);

        UUID subcategoryId = UUID.randomUUID();

        SubcategoryTotalDTO subcategory = new SubcategoryTotalDTO(
                subcategoryId,
                "Food",
                new BigDecimal("250.00"));

        when(reportService.getSubcategoryTotals(
                TransactionType.EXPENSE,
                from,
                to))
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
                .andExpect(jsonPath("$[0].total")
                        .value(250.00));

        verify(reportService)
                .getSubcategoryTotals(
                        TransactionType.EXPENSE,
                        from,
                        to);
    }

    @Test
    void shouldReturnBadRequestWhenPeriodIsInvalid()
            throws Exception {

        LocalDate from = LocalDate.of(2026, 10, 31);
        LocalDate to = LocalDate.of(2026, 10, 1);

        when(reportService.getPeriodTotal(from, to))
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
                        org.mockito.ArgumentMatchers.any());
    }
}