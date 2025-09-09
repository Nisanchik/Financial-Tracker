package ru.mirea.newrav1k.transactionservice.event;

import ru.mirea.newrav1k.transactionservice.model.enums.TransactionType;

import java.math.BigDecimal;
import java.util.UUID;

public record TransactionCreatedEvent(
        UUID eventId,
        UUID transactionId,
        UUID accountId,
        TransactionType type,
        BigDecimal amount
) {

}