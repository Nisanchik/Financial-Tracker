package ru.mirea.nisanchik.categoryservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mirea.nisanchik.categoryservice.event.publisher.CategoryEventPublisher;
import ru.mirea.nisanchik.categoryservice.exception.CategoryException;
import ru.mirea.nisanchik.categoryservice.exception.CategoryProcessingException;
import ru.mirea.nisanchik.categoryservice.exception.CategoryAccessDeniedException;
import ru.mirea.nisanchik.categoryservice.mapper.CategoryMapper;
import ru.mirea.nisanchik.categoryservice.model.dto.CategoryCreateRequest;
import ru.mirea.nisanchik.categoryservice.model.dto.CategoryResponse;
import ru.mirea.nisanchik.categoryservice.model.dto.CategoryUpdateRequest;
import ru.mirea.nisanchik.categoryservice.model.entity.Category;
import ru.mirea.nisanchik.categoryservice.model.enums.CategoryType;
import ru.mirea.nisanchik.categoryservice.repository.CategoryRepository;

import java.util.Objects;
import java.util.UUID;

import static ru.mirea.nisanchik.categoryservice.utils.MessageCode.*;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {
    private final CategoryRepository categoryRepository;

    private final CategoryMapper categoryMapper;

    private final CategoryEventPublisher categoryEventPublisher;

    public Page<CategoryResponse> findAll(Pageable pageable) {
        return categoryRepository.findAll(pageable).map(categoryMapper::toCategoryResponse);
    }

    public Page<CategoryResponse> findAllByTrackerID(UUID trackerId, Pageable pageable) {
        return categoryRepository.findAllByTrackerId(trackerId, pageable).map(categoryMapper::toCategoryResponse);
    }

    public CategoryResponse findById(UUID categoryId) {
        return categoryRepository.findById(categoryId).map(categoryMapper::toCategoryResponse).orElseThrow(() -> new CategoryException(CATEGORY_NOT_FOUND, HttpStatus.NOT_FOUND));
    }

    public CategoryResponse findByTrackerIdAndId(UUID trackerId, UUID categoryId) {
        return categoryRepository.findAllByTrackerIdAndId(trackerId, categoryId).map(categoryMapper::toCategoryResponse).orElseThrow(CategoryAccessDeniedException::new);
    }

    @Transactional
    public CategoryResponse create(UUID trackerId, CategoryCreateRequest request) {
        Category category = savePendingCategory(trackerId, request);

        return this.categoryMapper.toCategoryResponse(category);
    }

    @Transactional
    public void deleteById(UUID trackerId, UUID categoryId) {
        Category category = categoryRepository.findById(categoryId).orElseThrow();
        try {
            this.categoryEventPublisher.publishInternalCategoryDeletedEvent(categoryId);
        } catch (Exception e) {
            throw new CategoryProcessingException();
        }
        this.categoryRepository.delete(category);
    }

    @Transactional
    public CategoryResponse updateById(UUID trackerId, UUID categoryId, CategoryUpdateRequest request){
        return this.categoryRepository.findCategoryByTrackerIdAndId(trackerId, categoryId)
                .map(category -> {
                    if (request.name() != null) {
                        category.setName(request.name());
                    }
                    if (request.categoryType() != null) {
                        category.setType(request.categoryType());
                    }
                    return category;
                }).map(this.categoryMapper::toCategoryResponse).orElseThrow(CategoryAccessDeniedException::new);
    }

    @Transactional
    public CategoryResponse updateById(UUID trackerId, UUID categoryId, JsonNode jsonNode) {

        Category category = findCategoryByTrackerIdAndId(categoryId, trackerId);

        try {

            if (jsonNode.has("name")) {
                category.setName(jsonNode.get("name").asText());
            }

            if (jsonNode.has("type")) {
                CategoryType type = CategoryType.valueOf(jsonNode.get("type").asText());
                category.setType(type);
            }

            return categoryMapper.toCategoryResponse(category);

        } catch (Exception e) {
            throw new CategoryException(CATEGORY_UPDATE_FAILED, HttpStatus.BAD_REQUEST);
        }
    }

    private Category savePendingCategory(UUID trackerId, CategoryCreateRequest request) {
        Category category = buildCategoryFromRequest(trackerId, request);
        return categoryRepository.save(category);
    }


    private Category findCategoryByTrackerIdAndId(UUID trackerId, UUID categoryId) {
        return this.categoryRepository.findCategoryByTrackerIdAndId(trackerId, categoryId)
                .orElseThrow(CategoryAccessDeniedException::new);
    }

    private Category buildCategoryFromRequest(UUID trackerID, CategoryCreateRequest request) {
        Category category = new Category();

        category.setTrackerId(trackerID);
        category.setName(request.name());
        category.setType(request.type());
        category.setIsSystem(false);
        return category;
    }
}
