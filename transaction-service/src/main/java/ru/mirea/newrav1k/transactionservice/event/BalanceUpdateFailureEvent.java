package ru.mirea.newrav1k.transactionservice.event;

import java.util.UUID;

public record BalanceUpdateFailureEvent(
        UUID eventId,
        UUID transactionId,
        UUID accountId
) {

}