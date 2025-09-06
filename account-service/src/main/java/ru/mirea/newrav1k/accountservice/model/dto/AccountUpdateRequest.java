package ru.mirea.newrav1k.accountservice.model.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ru.mirea.newrav1k.accountservice.model.enums.AccountType;
import ru.mirea.newrav1k.accountservice.model.enums.Currency;

public record AccountUpdateRequest(
        @NotNull(message = "account.name.null")
        @Size(min = 3, max = 100, message = "account.name.invalid.length")
        String name,

        @NotNull(message = "account.currency.null")
        Currency currency,

        @NotNull(message = "account.type.null")
        AccountType type
) {

}