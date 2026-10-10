package com.mauro.cashiogram.transaction.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.mauro.cashiogram.transaction.entity.TransactionType;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateTransactionDTO(
        @NotNull @Positive BigDecimal amount,

        @NotNull TransactionType transactionType,

        @NotNull UUID categoryId,

        @NotNull LocalDate date,

        @Size(max = 255) String description,

        @Valid List<CreateTransactionSplitDTO> splits) {

}
