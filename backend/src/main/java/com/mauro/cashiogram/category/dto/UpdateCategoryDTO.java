package com.mauro.cashiogram.category.dto;

import jakarta.validation.constraints.Size;

public record UpdateCategoryDTO (
    @Size (max = 255, message = "must contain 255 characters at most")
    String name,

    @Size (max = 255, message = "must contain 255 characters at most")
    String description
) {
  
}
