package org.example.transactionservice.model.dto;

import org.example.transactionservice.model.enums.TransactionType;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record TransactionCreateRequest(
        UUID userId,
        BigDecimal amount,
        TransactionType type,
        UUID categoryId,
        UUID accountId,
        String description,
        List<String> tags
) {

}