package ru.mirea.nisanchik.categoryservice.controller.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import ru.mirea.nisanchik.categoryservice.CategoryServiceApplication;
import ru.mirea.nisanchik.categoryservice.model.dto.CategoryFilter;
import ru.mirea.nisanchik.categoryservice.model.dto.CategoryResponse;
import ru.mirea.nisanchik.categoryservice.model.entity.Category;
import ru.mirea.nisanchik.categoryservice.service.CategoryService;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/admin/categories")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class CategoryAdminController {

    private final CategoryService categoryService;

    public PagedModel<CategoryResponse> getAllCategories(@ModelAttribute CategoryFilter filter,
                                                       @PageableDefault Pageable pageable) {
        log.info("Admin request to get all categories");
        Page<CategoryResponse> categories = this.categoryService.findAll(filter, pageable);
        return new PagedModel<>(categories);
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable("categoryId") UUID categoryId) {
        log.info("Admin request to get category by id: categoryId={}", categoryId);
        CategoryResponse category = this.categoryService.findById(categoryId);
        return ResponseEntity.ok(category);
    }
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<Void> deleteCategoryById(@PathVariable("categoryId") UUID categoryId) {
        log.info("Admin request to delete category by id: categoryId={}", categoryId);
        this.categoryService.hardDeleteById(categoryId);
        return ResponseEntity.noContent().build();
    }

}
