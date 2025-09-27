package ru.mirea.newrav1k.accountservice.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import ru.mirea.newrav1k.accountservice.model.enums.AccountType;
import ru.mirea.newrav1k.accountservice.model.enums.Currency;

import java.math.BigDecimal;

public record AccountCreateRequest(
        @Schema(description = "Название аккаунта", example = "Основной счет", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "account.name.null")
        @Size(min = 3, max = 100, message = "account.name.invalid.length")
        String name,

        @Schema(description = "Валюта аккаунта", example = "RUB", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "account.currency.null")
        Currency currency,

        @Schema(description = "Тип аккаунта", example = "CREDIT_CARD", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "account.type.null")
        AccountType type,

        @Schema(description = "Кредитный лимит для аккаунта", example = "10000", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        @Positive(message = "error.account_credit_limit_cannot_be_negative")
        BigDecimal creditLimit
) {

}