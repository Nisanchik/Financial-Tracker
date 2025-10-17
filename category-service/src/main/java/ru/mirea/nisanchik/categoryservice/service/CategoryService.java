package ru.mirea.nisanchik.categoryservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mirea.nisanchik.categoryservice.event.publisher.CategoryEventPublisher;
import ru.mirea.nisanchik.categoryservice.exception.CategoryAccessDeniedException;
import ru.mirea.nisanchik.categoryservice.exception.CategoryNotFoundException;
import ru.mirea.nisanchik.categoryservice.exception.CategoryProcessingException;
import ru.mirea.nisanchik.categoryservice.exception.CategoryServiceException;
import ru.mirea.nisanchik.categoryservice.mapper.CategoryMapper;
import ru.mirea.nisanchik.categoryservice.model.dto.CategoryCreateRequest;
import ru.mirea.nisanchik.categoryservice.model.dto.CategoryFilter;
import ru.mirea.nisanchik.categoryservice.model.dto.CategoryResponse;
import ru.mirea.nisanchik.categoryservice.model.dto.CategoryUpdateRequest;
import ru.mirea.nisanchik.categoryservice.model.entity.Category;
import ru.mirea.nisanchik.categoryservice.repository.CategoryRepository;

import java.util.UUID;

import static ru.mirea.nisanchik.categoryservice.utils.MessageCode.CATEGORY_CREATE_FAILED;
import static ru.mirea.nisanchik.categoryservice.utils.MessageCode.CATEGORY_TYPE_CHANGE_FAILED;
import static ru.mirea.nisanchik.categoryservice.utils.MessageCode.CATEGORY_UPDATE_FAILED;
import static ru.mirea.nisanchik.categoryservice.utils.MessageCode.OPERATION_PROCESSING_FAILED;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    private final CategoryMapper categoryMapper;

    private final CategoryEventPublisher categoryEventPublisher;

    @PreAuthorize("hasRole('ADMIN')")
    public Page<CategoryResponse> findAll(CategoryFilter categoryFilter, Pageable pageable) {
        log.info("Find all categories");
        Specification<Category> specification = this.categoryRepository.buildSpecificationByFilter(categoryFilter);
        return this.categoryRepository.findAll(specification, pageable)
                .map(this.categoryMapper::toCategoryResponse);
    }

    @PreAuthorize("isAuthenticated()")
    public Page<CategoryResponse> findAllByTrackerId(UUID trackerId, CategoryFilter filter, Pageable pageable) {
        log.info("Find all categories");
        CategoryFilter updatedFilter = new CategoryFilter(trackerId, filter.type(), filter.name(), filter.isSystem());
        Specification<Category> specification = this.categoryRepository.buildSpecificationByFilter(updatedFilter);
        return this.categoryRepository.findAll(specification, pageable)
                .map(this.categoryMapper::toCategoryResponse);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public CategoryResponse findById(UUID categoryId) {
        log.info("Find category by ID");
        return this.categoryRepository.findById(categoryId)
                .map(this.categoryMapper::toCategoryResponse)
                .orElseThrow(CategoryNotFoundException::new);
    }

    @PreAuthorize("isAuthenticated()")
    public CategoryResponse findByTrackerIdAndId(UUID trackerId, UUID categoryId) {
        log.info("Find category by ID and tracker ID");
        return this.categoryRepository.findAllByTrackerIdAndId(trackerId, categoryId)
                .map(this.categoryMapper::toCategoryResponse)
                .orElseThrow(CategoryAccessDeniedException::new);
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional
    public CategoryResponse create(UUID trackerId, CategoryCreateRequest request) {
        log.info("Create category");
        try {
            Category category = savePendingCategory(trackerId, request);

            return this.categoryMapper.toCategoryResponse(category);
        } catch (DataIntegrityViolationException exception) {
            throw new CategoryProcessingException(CATEGORY_CREATE_FAILED);
        } catch (Exception exception) {
            throw new CategoryServiceException(OPERATION_PROCESSING_FAILED, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void hardDeleteById(UUID categoryId) {
        log.info("Delete category with id {}", categoryId);
        Category category = findCategoryById(categoryId);
        this.categoryEventPublisher.publishInternalCategoryDeletedEvent(categoryId);
        this.categoryRepository.delete(category);
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional
    public void softDeleteById(UUID trackerId, UUID categoryId) {
        log.info("Delete category with id {}", categoryId);
        Category category = findCategoryByTrackerIdAndId(trackerId, categoryId);
        this.categoryEventPublisher.publishInternalCategoryDeletedEvent(categoryId);
        category.setIsDeleted(true);
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional
    public CategoryResponse updateById(UUID trackerId, UUID categoryId, CategoryUpdateRequest request) {
        log.info("Update category with id {}", categoryId);
        return this.categoryRepository.findCategoryByTrackerIdAndId(trackerId, categoryId)
                .map(category -> {
                    if (request.name() != null) {
                        category.setName(request.name());
                    }
                    if (request.categoryType() != null) {
                        throw new CategoryServiceException(CATEGORY_TYPE_CHANGE_FAILED, HttpStatus.BAD_REQUEST);
                    }
                    return category;
                })
                .map(this.categoryMapper::toCategoryResponse)
                .orElseThrow(CategoryAccessDeniedException::new);
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional
    public CategoryResponse updateById(UUID trackerId, UUID categoryId, JsonNode jsonNode) {
        log.info("Update category with id {}", categoryId);
        Category category = findCategoryByTrackerIdAndId(categoryId, trackerId);

        try {
            if (jsonNode.has("name")) {
                category.setName(jsonNode.get("name").asText());
            }

            if (jsonNode.has("type")) {
                throw new CategoryServiceException(CATEGORY_TYPE_CHANGE_FAILED, HttpStatus.BAD_REQUEST);
            }

            return this.categoryMapper.toCategoryResponse(category);
        } catch (Exception e) {
            throw new CategoryServiceException(CATEGORY_UPDATE_FAILED, HttpStatus.BAD_REQUEST);
        }
    }

    private Category savePendingCategory(UUID trackerId, CategoryCreateRequest request) {
        log.info("Save pending category");
        Category category = buildCategoryFromRequest(trackerId, request);
        return this.categoryRepository.save(category);
    }

    private Category findCategoryById(UUID categoryId) {
        return this.categoryRepository.findById(categoryId)
                .orElseThrow(CategoryNotFoundException::new);
    }

    private Category findCategoryByTrackerIdAndId(UUID trackerId, UUID categoryId) {
        return this.categoryRepository.findCategoryByTrackerIdAndId(trackerId, categoryId)
                .orElseThrow(CategoryAccessDeniedException::new);
    }

    private Category buildCategoryFromRequest(UUID trackerId, CategoryCreateRequest request) {
        Category category = new Category();

        category.setTrackerId(trackerId);
        category.setName(request.name());
        category.setType(request.type());
        category.setIsSystem(false);

        return category;
    }

}