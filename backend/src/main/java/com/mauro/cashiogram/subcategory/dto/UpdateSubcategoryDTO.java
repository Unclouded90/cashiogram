package com.mauro.cashiogram.subcategory.dto;

import java.util.UUID;

import jakarta.validation.constraints.Size;

public record UpdateSubcategoryDTO (
    @Size(max = 255)
    String name,

    @Size(max = 255)
    String description,

    UUID categoryId
){
}
