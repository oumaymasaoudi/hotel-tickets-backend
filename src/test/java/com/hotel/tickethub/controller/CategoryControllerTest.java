package com.hotel.tickethub.controller;

import com.hotel.tickethub.dto.CategoryDTO;
import com.hotel.tickethub.dto.CategoryRequest;
import com.hotel.tickethub.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryController categoryController;

    private CategoryDTO categoryDTO;
    private CategoryRequest categoryRequest;

    @BeforeEach
    void setUp() {
        categoryDTO = new CategoryDTO();
        categoryDTO.setId(UUID.randomUUID());
        categoryDTO.setName("Test Category");

        categoryRequest = new CategoryRequest();
        categoryRequest.setName("New Category");
    }

    @Test
    void testGetAllCategories_Success() {
        // Given
        List<CategoryDTO> categories = Arrays.asList(categoryDTO);
        when(categoryService.getAllCategories()).thenReturn(categories);

        // When
        ResponseEntity<List<CategoryDTO>> response = categoryController.getAllCategories();

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Test Category", response.getBody().get(0).getName());
        verify(categoryService, times(1)).getAllCategories();
    }

    @Test
    void testGetAllCategories_Empty() {
        // Given
        when(categoryService.getAllCategories()).thenReturn(Collections.emptyList());

        // When
        ResponseEntity<List<CategoryDTO>> response = categoryController.getAllCategories();

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void testCreateCategory_Success() {
        // Given
        when(categoryService.createCategory(any(CategoryRequest.class))).thenReturn(categoryDTO);

        // When
        ResponseEntity<CategoryDTO> response = categoryController.createCategory(categoryRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Test Category", response.getBody().getName());
        verify(categoryService, times(1)).createCategory(any(CategoryRequest.class));
    }

    @Test
    void testCreateCategory_Error() {
        // Given
        when(categoryService.createCategory(any(CategoryRequest.class)))
                .thenThrow(new RuntimeException("Error"));

        // When
        ResponseEntity<CategoryDTO> response = categoryController.createCategory(categoryRequest);

        // Then
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
