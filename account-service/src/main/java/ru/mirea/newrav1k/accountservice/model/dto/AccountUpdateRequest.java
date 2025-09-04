package ru.mirea.newrav1k.accountservice.model.dto;

import ru.mirea.newrav1k.accountservice.model.enums.AccountType;
import ru.mirea.newrav1k.accountservice.model.enums.Currency;

public record AccountUpdateRequest(
        String name,
        Currency currency,
        AccountType type
) {

}