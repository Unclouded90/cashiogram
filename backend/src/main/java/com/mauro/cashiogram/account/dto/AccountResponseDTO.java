package com.mauro.cashiogram.account.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AccountResponseDTO(
        UUID id,
        BigDecimal initialBalance,
        Instant createdAt,
        Instant updatedAt) {

}
