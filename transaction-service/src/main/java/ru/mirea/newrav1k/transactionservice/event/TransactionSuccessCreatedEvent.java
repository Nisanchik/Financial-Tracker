package ru.mirea.newrav1k.transactionservice.event;

import java.util.UUID;

public record TransactionSuccessCreatedEvent(
        UUID eventId,
        UUID transactionId
) {

}