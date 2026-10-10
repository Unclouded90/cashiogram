package com.mauro.cashiogram.transaction.dto;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.Positive;

public record UpdateTransactionSplitDTO(

        UUID subcategoryId,

        @Positive BigDecimal amount

) {
}