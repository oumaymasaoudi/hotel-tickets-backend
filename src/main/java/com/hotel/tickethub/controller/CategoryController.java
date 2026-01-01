package com.hotel.tickethub.controller;

import com.hotel.tickethub.dto.CategoryDTO;
import com.hotel.tickethub.dto.CategoryRequest;
import com.hotel.tickethub.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@CrossOrigin(origins = {
        "http://localhost:8080",
        "http://localhost:8081",
        "http://localhost:5173",
        "http://192.168.58.1:5173",
        "http://51.21.196.104"
})
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * GET /api/categories/public - Récupérer toutes les catégories (public)
     * Retourne des CategoryDTO pour éviter les références circulaires
     */
    @GetMapping("/public")
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    /**
     * POST /api/categories - Créer une nouvelle catégorie
     * Accessible par: SuperAdmin uniquement
     */
    @PostMapping
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<CategoryDTO> createCategory(@RequestBody CategoryRequest request) {
        try {
            CategoryDTO created = categoryService.createCategory(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
