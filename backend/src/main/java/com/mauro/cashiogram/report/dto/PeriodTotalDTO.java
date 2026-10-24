package com.mauro.cashiogram.report.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PeriodTotalDTO(
        LocalDate from,
        LocalDate to,
        BigDecimal totalIncome,
        BigDecimal totalExpenses,
        BigDecimal netTotal) {
}