package ru.mirea.nisanchik.categoryservice.model.dto;

import jakarta.validation.constraints.NotNull;
import ru.mirea.nisanchik.categoryservice.model.enums.CategoryType;

import java.util.UUID;

public record CategoryCreateRequest(String name, @NotNull(message = "error.category_type_validate_failed")
CategoryType type) {

}
