package com.mauro.cashiogram.subcategory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateSubcategoryDTO (
    @NotBlank 
    @Size(max = 255)
    String name,

    @Size(max = 255)
    String description
){
}
