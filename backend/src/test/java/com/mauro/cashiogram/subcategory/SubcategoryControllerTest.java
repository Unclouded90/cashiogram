package com.mauro.cashiogram.subcategory;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.mauro.cashiogram.exception.BadRequestException;
import com.mauro.cashiogram.exception.ResourceNotFoundException;
import com.mauro.cashiogram.subcategory.dto.CreateSubcategoryDTO;
import com.mauro.cashiogram.subcategory.dto.SubcategoryResponseDTO;
import com.mauro.cashiogram.subcategory.dto.UpdateSubcategoryDTO;

import tools.jackson.databind.json.JsonMapper;

@WebMvcTest(SubcategoryController.class)
public class SubcategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonMapper objectMapper;

    @MockitoBean
    private SubcategoryService subcategoryService;

    private final UUID groceriesId = UUID.randomUUID();

    private final SubcategoryResponseDTO cats = new SubcategoryResponseDTO(
            UUID.randomUUID(),
            "Cats",
            "Cat related purchases",
            groceriesId,
            Instant.now());

    private final SubcategoryResponseDTO food = new SubcategoryResponseDTO(
            UUID.randomUUID(),
            "Food",
            "Food related purchases",
            groceriesId,
            Instant.now());

    @Test
    void itShouldGetAllSubcategories() throws Exception {
        when(subcategoryService.getSubcategories(null, null))
                .thenReturn(List.of(cats, food));

        mockMvc.perform(get("/subcategories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Cats"))
                .andExpect(jsonPath("$[1].name").value("Food"));

        verify(subcategoryService).getSubcategories(null, null);
    }

    @Test
    void itShouldGetSubcategoriesFilteredByCategoryAndName() throws Exception {
        when(subcategoryService.getSubcategories(groceriesId, "food"))
                .thenReturn(List.of(food));

        mockMvc.perform(get("/subcategories")
                .param("categoryId", groceriesId.toString())
                .param("name", "food"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Food"))
                .andExpect(jsonPath("$[0].categoryId")
                        .value(groceriesId.toString()));

        verify(subcategoryService)
                .getSubcategories(groceriesId, "food");
    }

    @Test
    void itShouldGetSubcategoryById() throws Exception {
        UUID id = cats.id();

        when(subcategoryService.getSubcategoryById(id))
                .thenReturn(cats);

        mockMvc.perform(get("/subcategories/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Cats"))
                .andExpect(jsonPath("$.description")
                        .value("Cat related purchases"))
                .andExpect(jsonPath("$.categoryId")
                        .value(groceriesId.toString()));

        verify(subcategoryService).getSubcategoryById(id);
    }

    @Test
    void itShouldReturn404WhenSubcategoryIsNotFound() throws Exception {
        UUID id = UUID.randomUUID();

        when(subcategoryService.getSubcategoryById(id))
                .thenThrow(
                        new ResourceNotFoundException("Subcategory not found"));

        mockMvc.perform(get("/subcategories/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message")
                        .value("Subcategory not found"));

        verify(subcategoryService).getSubcategoryById(id);
    }

    @Test
    void itShouldCreateSubcategory() throws Exception {
        CreateSubcategoryDTO request = new CreateSubcategoryDTO(
                "Cleaning",
                "Cleaning products");

        SubcategoryResponseDTO response = new SubcategoryResponseDTO(
                UUID.randomUUID(),
                "Cleaning",
                "Cleaning products",
                groceriesId,
                Instant.now());

        when(subcategoryService.createSubcategory(groceriesId, request))
                .thenReturn(response);

        mockMvc.perform(post(
                "/categories/{categoryId}/subcategories",
                groceriesId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Cleaning"))
                .andExpect(jsonPath("$.description")
                        .value("Cleaning products"))
                .andExpect(jsonPath("$.categoryId")
                        .value(groceriesId.toString()));

        verify(subcategoryService)
                .createSubcategory(groceriesId, request);
    }

    @Test
    void itShouldReturn400WhenCreatingSubcategoryWithNoName()
            throws Exception {

        CreateSubcategoryDTO request = new CreateSubcategoryDTO(
                "",
                "Invalid subcategory");

        mockMvc.perform(post(
                "/categories/{categoryId}/subcategories",
                groceriesId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(subcategoryService, never())
                .createSubcategory(
                        org.mockito.ArgumentMatchers.eq(groceriesId),
                        any(CreateSubcategoryDTO.class));
    }

    @Test
    void itShouldUpdateSubcategory() throws Exception {
        UUID id = cats.id();

        UpdateSubcategoryDTO request = new UpdateSubcategoryDTO(
                "Cat Products",
                "Food, litter and other cat products",
                null);

        when(subcategoryService.updateSubcategory(id, request))
                .thenReturn(new SubcategoryResponseDTO(
                        id,
                        "Cat Products",
                        "Food, litter and other cat products",
                        groceriesId,
                        cats.createdAt()));

        mockMvc.perform(patch("/subcategories/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Cat Products"))
                .andExpect(jsonPath("$.description")
                        .value("Food, litter and other cat products"))
                .andExpect(jsonPath("$.categoryId")
                        .value(groceriesId.toString()));

        verify(subcategoryService)
                .updateSubcategory(id, request);
    }

    @Test
    void itShouldFailToUpdateSubcategoryIfNameIsDuplicated()
            throws Exception {

        UUID id = cats.id();

        UpdateSubcategoryDTO request = new UpdateSubcategoryDTO(
                "Food",
                null,
                null);

        when(subcategoryService.updateSubcategory(id, request))
                .thenThrow(
                        new BadRequestException(
                                "Subcategory already exists in that category"));

        mockMvc.perform(patch("/subcategories/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message")
                        .value("Subcategory already exists in that category"));

        verify(subcategoryService)
                .updateSubcategory(id, request);
    }

    @Test
    void itShouldDeleteSubcategory() throws Exception {
        UUID id = cats.id();

        mockMvc.perform(delete("/subcategories/{id}", id))
                .andExpect(status().isNoContent());

        verify(subcategoryService).deleteSubcategory(id);
    }
}