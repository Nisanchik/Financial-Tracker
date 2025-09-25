package ru.mirea.newrav1k.accountservice.model.dto;

import ru.mirea.newrav1k.accountservice.model.enums.Currency;

import java.time.Instant;
import java.util.UUID;

public record AccountFilter(
        UUID trackerId,
        String name,
        Currency currency,
        Instant createdAtFrom,
        Instant createdAtTo
) {

}