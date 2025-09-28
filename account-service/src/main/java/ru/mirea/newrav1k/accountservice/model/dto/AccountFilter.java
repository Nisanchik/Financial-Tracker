package ru.mirea.newrav1k.accountservice.model.dto;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Parameter;
import ru.mirea.newrav1k.accountservice.model.enums.Currency;

import java.time.Instant;
import java.util.UUID;

public record AccountFilter(
        @Hidden
        UUID trackerId,
        @Parameter(name = "name", description = "Имя аккаунта", example = "Основной аккаунт")
        String name,
        @Parameter(name = "currency", description = "Валюта аккаунта", example = "RUB")
        Currency currency,
        @Parameter(name = "createdAtFrom", description = "Дата создания от (включительно)", example = "2024-01-01T00:00:00Z")
        Instant createdAtFrom,
        @Parameter(name = "createdAtTo", description = "Дата создания до (включительно)", example = "2024-12-31T23:59:59Z")
        Instant createdAtTo
) {

}