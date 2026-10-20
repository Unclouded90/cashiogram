package com.mauro.cashiogram.balance.dto;

import java.math.BigDecimal;

public record BalanceResponseDTO(
        BigDecimal initialBalance,
        BigDecimal totalIncome,
        BigDecimal totalExpenses,
        BigDecimal currentBalance) {
}
