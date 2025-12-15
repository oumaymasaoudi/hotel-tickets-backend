package com.hotel.tickethub.service;

import com.hotel.tickethub.dto.CategoryDTO;
import com.hotel.tickethub.dto.CategoryRequest;
import com.hotel.tickethub.model.Category;
import com.hotel.tickethub.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public CategoryDTO getCategoryById(UUID id) {
        return categoryRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new RuntimeException("Category not found"));
    }

    @Transactional
    public CategoryDTO createCategory(CategoryRequest request) {
        // Vérifier si une catégorie avec le même nom existe déjà
        if (categoryRepository.findByName(request.getName()).isPresent()) {
            throw new RuntimeException("Une catégorie avec ce nom existe déjà");
        }

        Category category = new Category();
        category.setName(request.getName());
        category.setIcon(request.getIcon() != null ? request.getIcon() : "Package");
        category.setColor(request.getColor() != null ? request.getColor() : "#6C757D");
        category.setIsMandatory(request.getIsMandatory() != null ? request.getIsMandatory() : false);
        category.setAdditionalCost(request.getAdditionalCost() != null ? request.getAdditionalCost() : BigDecimal.ZERO);

        Category saved = categoryRepository.save(category);
        return convertToDTO(saved);
    }

    private CategoryDTO convertToDTO(Category category) {
        return CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .icon(category.getIcon())
                .color(category.getColor())
                .isMandatory(category.getIsMandatory())
                .additionalCost(category.getAdditionalCost())
                .build();
    }
}
