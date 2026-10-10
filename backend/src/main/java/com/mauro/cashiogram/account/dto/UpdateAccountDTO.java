package com.mauro.cashiogram.account.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;

public record UpdateAccountDTO(
        @NotNull BigDecimal initialBalance) {

}
