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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import ru.mirea.nisanchik.categoryservice.model.dto.CategoryCreateRequest;
import ru.mirea.nisanchik.categoryservice.model.dto.CategoryFilter;
import ru.mirea.nisanchik.categoryservice.model.dto.CategoryResponse;
import ru.mirea.nisanchik.categoryservice.model.dto.CategoryUpdateRequest;
import ru.mirea.nisanchik.categoryservice.security.HeaderAuthenticationDetails;
import ru.mirea.nisanchik.categoryservice.service.CategoryService;

import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public PagedModel<CategoryResponse> getAllCategories(@AuthenticationPrincipal HeaderAuthenticationDetails authenticationDetails,
                                                         @PageableDefault Pageable pageable,
                                                         @ModelAttribute CategoryFilter categoryFilter) {
        log.info("getAllCategories");
        Page<CategoryResponse> categories =
                this.categoryService.findAllByTrackerId(authenticationDetails.getTrackerId(), categoryFilter, pageable);
        return new PagedModel<>(categories);
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse> getCategory(@AuthenticationPrincipal HeaderAuthenticationDetails authenticationDetails,
                                                        @PathVariable("categoryId") UUID categoryId) {
        log.info("getCategory");
        CategoryResponse category = this.categoryService.findByTrackerIdAndId(authenticationDetails.getTrackerId(), categoryId);
        return new ResponseEntity<>(category, HttpStatus.OK);
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@AuthenticationPrincipal HeaderAuthenticationDetails authenticationDetails,
                                                           @Valid @RequestBody CategoryCreateRequest request,
                                                           UriComponentsBuilder uriBuilder) {
        log.info("createCategory");
        CategoryResponse category = this.categoryService.create(authenticationDetails.getTrackerId(), request);
        return ResponseEntity.created(uriBuilder
                .replacePath("/api/category/{categoryId}")
                .build(category.id())).body(category);
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse> updateCategory(@AuthenticationPrincipal HeaderAuthenticationDetails authenticationDetails,
                                                           @PathVariable("categoryId") UUID transactionId,
                                                           @Valid @RequestBody CategoryUpdateRequest request) {
        log.info("updateCategory");
        CategoryResponse category = this.categoryService.updateById(authenticationDetails.getTrackerId(), transactionId, request);
        return ResponseEntity.ok(category);
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse> partialUpdateCategory(@AuthenticationPrincipal HeaderAuthenticationDetails authenticationDetails,
                                                                  @PathVariable("categoryId") UUID transactionId,
                                                                  @RequestBody JsonNode jsonNode) {
        log.info("partialUpdateCategory");
        CategoryResponse category = this.categoryService.updateById(authenticationDetails.getTrackerId(), transactionId, jsonNode);
        return ResponseEntity.ok(category);
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{categoryId}")
    public ResponseEntity<CategoryResponse> deleteCategory(@AuthenticationPrincipal HeaderAuthenticationDetails authenticationDetails,
                                                           @PathVariable("categoryId") UUID categoryId) {
        log.info("Delete category with id {}", categoryId);
        this.categoryService.softDeleteById(authenticationDetails.getTrackerId(), categoryId);
        return ResponseEntity.noContent().build();
    }
}
