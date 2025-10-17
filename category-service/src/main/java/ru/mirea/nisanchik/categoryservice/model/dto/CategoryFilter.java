package ru.mirea.nisanchik.categoryservice.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import ru.mirea.nisanchik.categoryservice.model.enums.CategoryType;

public record CategoryFilter (@JsonProperty(value="type") CategoryType type, Boolean isSystem){
}
