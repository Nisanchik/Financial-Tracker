package ru.mirea.nisanchik.categoryservice.controller.rest;


import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import ru.mirea.nisanchik.categoryservice.model.dto.CategoryCreateRequest;
import ru.mirea.nisanchik.categoryservice.model.dto.CategoryFilter;
import ru.mirea.nisanchik.categoryservice.model.dto.CategoryResponse;
import ru.mirea.nisanchik.categoryservice.model.dto.CategoryUpdateRequest;
import ru.mirea.nisanchik.categoryservice.service.CategoryService;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping()
    public PagedModel<CategoryResponse> getAllCategories(@PageableDefault Pageable pageable, @ModelAttribute CategoryFilter categoryFilter) {
        log.info("getAllCategories");
        Page<CategoryResponse> categories =
                this.categoryService.findAll(categoryFilter, pageable);
        return new PagedModel<>(categories);
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse> getCategory(@PathVariable("categoryId") UUID categoryId) {
        log.info("getCategory");
        CategoryResponse category = this.categoryService.findById(categoryId);
        return new ResponseEntity<>(category, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryCreateRequest request,
                                                           UriComponentsBuilder uriBuilder) {
        log.info("createCategory");
        CategoryResponse category = this.categoryService.create(UUID.randomUUID(), request);
        return ResponseEntity.created(uriBuilder
                .replacePath("/api/category/{categoryId}")
                .build(category.id())).body(category);
    }

    @PutMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse> updateCategory(@PathVariable("categoryId") UUID transactionId,
                                                           @Valid @RequestBody CategoryUpdateRequest request){
        log.info("updateCategory");
        CategoryResponse category = this.categoryService.updateById(UUID.randomUUID(), transactionId, request);
        return ResponseEntity.ok(category);
    }

    @PatchMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse> partialUpdateCategory(@PathVariable("categoryId") UUID transactionId,
                                                                  @RequestBody JsonNode jsonNode){
        log.info("partialUpdateCategory");
        CategoryResponse category = this.categoryService.updateById(UUID.randomUUID(), transactionId, jsonNode);
        return ResponseEntity.ok(category);
    }

    @DeleteMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse> deleteCategory(@PathVariable("categoryId") UUID categoryId) {
        log.info("Delete category with id {}", categoryId);
        this.categoryService.hardDeleteById(categoryId);
        return ResponseEntity.noContent().build();
    }
}
