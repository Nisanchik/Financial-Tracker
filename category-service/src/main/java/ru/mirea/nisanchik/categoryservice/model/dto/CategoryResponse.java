package ru.mirea.nisanchik.categoryservice.model.dto;


import ru.mirea.nisanchik.categoryservice.model.enums.CategoryType;

import java.util.UUID;

public record CategoryResponse(UUID id, UUID trackerId, String name, CategoryType type, boolean isSystem) {

}
