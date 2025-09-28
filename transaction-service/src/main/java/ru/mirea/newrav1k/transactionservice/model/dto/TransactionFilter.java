package ru.mirea.newrav1k.transactionservice.model.dto;

import java.time.Instant;
import java.util.UUID;

public record TransactionFilter(
        UUID trackerId,
        String type,
        Instant createdAtFrom,
        Instant createdAtTo
) {

}