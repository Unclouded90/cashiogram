package com.mauro.cashiogram.transaction.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.mauro.cashiogram.transaction.entity.TransactionType;

public record TransactionResponseDTO(
        UUID id,
        BigDecimal amount,
        TransactionType transactionType,
        UUID categoryId,
        LocalDate date,
        String description,
        List<TransactionSplitResponseDTO> splits,
        Instant createdAt) {

}
