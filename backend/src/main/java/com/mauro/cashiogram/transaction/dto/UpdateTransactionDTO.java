package com.mauro.cashiogram.transaction.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.mauro.cashiogram.transaction.entity.TransactionType;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record UpdateTransactionDTO(

        @Positive BigDecimal amount,

        TransactionType transactionType,

        UUID categoryId,

        LocalDate date,

        @Size(max = 255) String description

) {
}