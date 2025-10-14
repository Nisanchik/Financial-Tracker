package ru.mirea.nisanchik.categoryservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import ru.mirea.nisanchik.categoryservice.model.dto.CategoryResponse;
import ru.mirea.nisanchik.categoryservice.model.entity.Category;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface CategoryMapper {
    CategoryResponse toCategoryResponse(Category category);
}
