package com.hotel.tickethub.service;

import com.hotel.tickethub.dto.CategoryDTO;
import com.hotel.tickethub.dto.CategoryRequest;
import com.hotel.tickethub.model.Category;
import com.hotel.tickethub.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category testCategory;
    private UUID testCategoryId;

    @BeforeEach
    void setUp() {
        testCategoryId = UUID.randomUUID();

        testCategory = new Category();
        testCategory.setId(testCategoryId);
        testCategory.setName("Test Category");
        testCategory.setIcon("Package");
        testCategory.setColor("#6C757D");
        testCategory.setIsMandatory(false);
        testCategory.setAdditionalCost(BigDecimal.ZERO);
    }

    @Test
    void testGetAllCategories_Success() {
        Category category2 = new Category();
        category2.setId(UUID.randomUUID());
        category2.setName("Category 2");
        category2.setIcon("Wrench");
        category2.setColor("#FF0000");
        category2.setIsMandatory(true);
        category2.setAdditionalCost(new BigDecimal("10.00"));

        when(categoryRepository.findAll()).thenReturn(Arrays.asList(testCategory, category2));

        List<CategoryDTO> result = categoryService.getAllCategories();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Test Category", result.get(0).getName());
        assertEquals("Category 2", result.get(1).getName());
        verify(categoryRepository, times(1)).findAll();
    }

    @Test
    void testGetAllCategories_Empty() {
        when(categoryRepository.findAll()).thenReturn(java.util.Collections.emptyList());

        List<CategoryDTO> result = categoryService.getAllCategories();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(categoryRepository, times(1)).findAll();
    }

    @Test
    void testGetCategoryById_Success() {
        when(categoryRepository.findById(testCategoryId)).thenReturn(Optional.of(testCategory));

        CategoryDTO result = categoryService.getCategoryById(testCategoryId);

        assertNotNull(result);
        assertEquals(testCategoryId, result.getId());
        assertEquals("Test Category", result.getName());
        assertEquals("Package", result.getIcon());
        verify(categoryRepository, times(1)).findById(testCategoryId);
    }

    @Test
    void testGetCategoryById_NotFound() {
        UUID nonExistentId = UUID.randomUUID();
        when(categoryRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            categoryService.getCategoryById(nonExistentId);
        });

        assertEquals("Category not found", exception.getMessage());
        verify(categoryRepository, times(1)).findById(nonExistentId);
    }

    @Test
    void testCreateCategory_Success() {
        CategoryRequest request = new CategoryRequest();
        request.setName("New Category");
        request.setIcon("Wrench");
        request.setColor("#FF0000");
        request.setIsMandatory(true);
        request.setAdditionalCost(new BigDecimal("15.50"));

        when(categoryRepository.findByName("New Category")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category category = invocation.getArgument(0);
            category.setId(UUID.randomUUID());
            return category;
        });

        CategoryDTO result = categoryService.createCategory(request);

        assertNotNull(result);
        assertEquals("New Category", result.getName());
        assertEquals("Wrench", result.getIcon());
        assertEquals("#FF0000", result.getColor());
        assertTrue(result.getIsMandatory());
        assertEquals(new BigDecimal("15.50"), result.getAdditionalCost());
        verify(categoryRepository, times(1)).findByName("New Category");
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void testCreateCategory_WithDefaults() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Category With Defaults");
        request.setIcon(null);
        request.setColor(null);
        request.setIsMandatory(null);
        request.setAdditionalCost(null);

        when(categoryRepository.findByName("Category With Defaults")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category category = invocation.getArgument(0);
            category.setId(UUID.randomUUID());
            return category;
        });

        CategoryDTO result = categoryService.createCategory(request);

        assertNotNull(result);
        assertEquals("Category With Defaults", result.getName());
        assertEquals("Package", result.getIcon());
        assertEquals("#6C757D", result.getColor());
        assertFalse(result.getIsMandatory());
        assertEquals(BigDecimal.ZERO, result.getAdditionalCost());
        verify(categoryRepository, times(1)).save(any(Category.class));
    }

    @Test
    void testCreateCategory_DuplicateName() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Existing Category");

        when(categoryRepository.findByName("Existing Category")).thenReturn(Optional.of(testCategory));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            categoryService.createCategory(request);
        });

        assertTrue(exception.getMessage().contains("existe déjà"));
        verify(categoryRepository, times(1)).findByName("Existing Category");
        verify(categoryRepository, never()).save(any(Category.class));
    }
}
