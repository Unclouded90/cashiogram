package com.mauro.cashiogram.category;

import org.springframework.http.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.mauro.cashiogram.category.dto.CategoryResponseDTO;
import com.mauro.cashiogram.category.dto.CreateCategoryDTO;
import com.mauro.cashiogram.category.dto.UpdateCategoryDTO;
import com.mauro.cashiogram.exception.BadRequestException;
import com.mauro.cashiogram.exception.ResourceNotFoundException;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import tools.jackson.databind.json.JsonMapper;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;;

@WebMvcTest(CategoryController.class)
public class CategoryControllerTest {
    @Autowired 
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper objectMapper;

    @MockitoBean  
    private CategoryService categoryService;

    private CategoryResponseDTO groceries = new CategoryResponseDTO(
        UUID.randomUUID(),
        "Groceries",
        "General purchases",
        Instant.now()
    );

    private CategoryResponseDTO restaurants = new CategoryResponseDTO(
        UUID.randomUUID(),
        "Restaurants",
        "Eating out or takeaway",
        Instant.now()
    );

    @Test 
    void itShouldGetAllCategories() throws Exception {
         when(categoryService.getCategories(null))
            .thenReturn(List.of(groceries));

        mockMvc.perform(get("/categories"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].name").value("Groceries"));
        
        verify(categoryService).getCategories(null);
    }

    @Test 
    void itShouldGetCategoryById() throws Exception {
        UUID id = restaurants.id();

        when(categoryService.getCategoryById(id))
            .thenReturn(restaurants);

        mockMvc.perform(get("/categories/{id}", id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id.toString()))
            .andExpect(jsonPath("$.name").value("Restaurants"))
            .andExpect(jsonPath("$.description").value("Eating out or takeaway"));

        verify(categoryService).getCategoryById(id);
    }

    @Test
    void itShouldGetCategoriesFilteredByName() throws Exception {
        when(categoryService.getCategories("groceries"))
            .thenReturn(List.of(groceries));
    
        mockMvc.perform(get("/categories")
                .param("name", "groceries"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].name").value("Groceries"));
    
        verify(categoryService).getCategories("groceries");
    }
  
    @Test 
    void itShouldReturnError() throws Exception {
        UUID id = UUID.randomUUID();

        when(categoryService.getCategoryById(id))
            .thenThrow(new ResourceNotFoundException("Category not found"));

        mockMvc.perform(get("/categories/{id}", id))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").value("Category not found"));
    }

    @Test 
    void itShouldCreateCategory() throws Exception {
        final CreateCategoryDTO fuel = new CreateCategoryDTO(
            "Fuel",
            "Monthly fuel related expenses"
        );

        when(categoryService.createCategory(fuel))
            .thenReturn(new CategoryResponseDTO(
                UUID.randomUUID(),
                "Fuel",
                "Monthly fuel related expenses",
                Instant.now()
            ));

        mockMvc.perform(post("/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(fuel)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Fuel"))
            .andExpect(jsonPath("$.description").value("Monthly fuel related expenses"));
    }

    @Test 
    void itShouldReturn400WhenCreatingWithNoName() throws Exception {
        final CreateCategoryDTO test = new CreateCategoryDTO(
            "",
            "Test expenses"
        );

        mockMvc.perform(post("/categories")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(test)))
            .andExpect(status().isBadRequest());

        verify(categoryService, never()).createCategory(any(CreateCategoryDTO.class));
    }

    @Test 
    void itShouldUpdateCategory() throws Exception {
        UpdateCategoryDTO fuel = new UpdateCategoryDTO(
            "Fuel",
            "Monthly fuel related expenses"
        );

        UUID id = restaurants.id();

        when(categoryService.updateCategory(id, fuel))
            .thenReturn(new CategoryResponseDTO(
                restaurants.id(),
                "Fuel",
                "Monthly fuel related expenses",
                restaurants.createdAt()
            ));

        mockMvc.perform(patch("/categories/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(fuel)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id.toString()))
            .andExpect(jsonPath("$.name").value("Fuel"))
            .andExpect(jsonPath("$.description").value("Monthly fuel related expenses"))
            .andExpect(jsonPath("$.createdAt").value(restaurants.createdAt().toString()));

        verify(categoryService).updateCategory(id, fuel);
    }

    @Test 
    void itShouldFailToUpdateCategoryIfNameIsDuplicated() throws Exception {
        UpdateCategoryDTO test = new UpdateCategoryDTO(
            "Restaurants",
            "Orders at GLOVO"
        );

        UUID id = restaurants.id();

        when(categoryService.updateCategory(id, test))
            .thenThrow(new BadRequestException("Category already exists"));

        mockMvc.perform(patch("/categories/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(test)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.status").value(400))
            .andExpect(jsonPath("$.message").value("Category already exists"));

        verify(categoryService).updateCategory(id, test);
    }

    @Test 
    void itShouldDeleteCategory() throws Exception {
        UUID id = restaurants.id();

        mockMvc.perform(delete("/categories/{id}", id))
            .andExpect(status().isNoContent());

        verify(categoryService).deleteCategory(id);
    }
}
