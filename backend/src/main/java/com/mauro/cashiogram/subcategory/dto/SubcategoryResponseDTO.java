package com.mauro.cashiogram.subcategory.dto;

import java.time.Instant;
import java.util.UUID;

public record SubcategoryResponseDTO (
    UUID id,
    String name,
    String description,
    UUID categoryId,
    Instant createdAt
){
}
