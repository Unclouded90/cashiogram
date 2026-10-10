package com.mauro.cashiogram.transaction.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record TransactionSplitResponseDTO(
        UUID id,
        UUID subcategoryId,
        BigDecimal amount) {

}
