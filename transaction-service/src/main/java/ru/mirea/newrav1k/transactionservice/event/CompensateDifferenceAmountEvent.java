package ru.mirea.newrav1k.transactionservice.event;

import ru.mirea.newrav1k.transactionservice.model.enums.TransactionType;

import java.math.BigDecimal;
import java.util.UUID;

public record CompensateDifferenceAmountEvent(
        UUID compensationId,
        UUID transactionId,
        UUID accountId,
        TransactionType transactionType,
        BigDecimal oldAmount,
        BigDecimal newAmount
) {

}