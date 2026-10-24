package com.mauro.cashiogram.report.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record SubcategoryTotalDTO(
        UUID subcategoryId,
        String subcategoryName,
        BigDecimal total) {

}
