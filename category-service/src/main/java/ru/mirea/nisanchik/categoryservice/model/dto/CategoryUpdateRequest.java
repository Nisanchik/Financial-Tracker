package ru.mirea.nisanchik.categoryservice.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ru.mirea.nisanchik.categoryservice.model.enums.CategoryType;

public record CategoryUpdateRequest (
        @NotNull(message = "error.category_type_validate_failed")
        CategoryType categoryType,

        @Size(min=3, message = "error.category_name_size_failed")
        String name
){
}
