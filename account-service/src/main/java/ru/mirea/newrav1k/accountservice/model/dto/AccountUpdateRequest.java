package ru.mirea.newrav1k.accountservice.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ru.mirea.newrav1k.accountservice.model.enums.Currency;

public record AccountUpdateRequest(
        @Schema(description = "Новое название аккаунта", example = "Обновленное название", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "account.name.null")
        @Size(min = 3, max = 100, message = "account.name.invalid.length")
        String name,

        @Schema(description = "Новая валюта аккаунта", example = "EUR", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "account.currency.null")
        Currency currency
) {

}