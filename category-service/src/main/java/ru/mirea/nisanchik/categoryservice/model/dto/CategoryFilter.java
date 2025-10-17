package ru.mirea.nisanchik.categoryservice.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.mirea.nisanchik.categoryservice.model.enums.CategoryType;

import java.util.UUID;

public record CategoryFilter(
        UUID trackerId,
        @JsonProperty(value = "type") CategoryType type,
        Boolean isSystem
) {
}