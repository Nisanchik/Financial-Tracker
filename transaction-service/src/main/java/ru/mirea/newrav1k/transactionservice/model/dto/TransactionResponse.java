package ru.mirea.newrav1k.transactionservice.model.dto;

import ru.mirea.newrav1k.transactionservice.model.enums.TransactionType;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record TransactionResponse(
        UUID id,
        UUID trackerId,
        BigDecimal amount,
        TransactionType type,
        UUID categoryId,
        UUID accountId,
        String description,
        List<String> tags
) {

}