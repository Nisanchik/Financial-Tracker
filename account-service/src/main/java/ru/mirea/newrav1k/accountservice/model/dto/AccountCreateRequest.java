package ru.mirea.newrav1k.accountservice.model.dto;

import ru.mirea.newrav1k.accountservice.model.enums.AccountType;
import ru.mirea.newrav1k.accountservice.model.enums.Currency;

import java.util.UUID;

public record AccountCreateRequest(
        UUID userId,
        String name,
        Currency currency,
        AccountType type
) {

}