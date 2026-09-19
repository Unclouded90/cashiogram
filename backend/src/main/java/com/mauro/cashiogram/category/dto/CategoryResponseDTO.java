package com.mauro.cashiogram.category.dto;

import java.time.Instant;
import java.util.UUID;

public record CategoryResponseDTO (
    UUID id,
    String name,
    String description,
    Instant createdAt
){
  
}
