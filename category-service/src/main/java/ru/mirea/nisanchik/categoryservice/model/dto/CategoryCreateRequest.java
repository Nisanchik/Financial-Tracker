package ru.mirea.nisanchik.categoryservice.model.dto;

import ru.mirea.nisanchik.categoryservice.model.enums.CategoryType;

import java.util.UUID;

public record CategoryCreateRequest(String name, CategoryType type) {
}
