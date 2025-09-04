package ru.mirea.newrav1k.accountservice.model.dto;

import ru.mirea.newrav1k.accountservice.model.enums.AccountType;
import ru.mirea.newrav1k.accountservice.model.enums.Currency;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountResponse(
        UUID id,
        String name,
        BigDecimal balance,
        Currency currency,
        AccountType type,
        boolean active
) {

}