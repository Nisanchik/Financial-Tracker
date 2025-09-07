package ru.mirea.newrav1k.transactionservice.model.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import ru.mirea.newrav1k.transactionservice.model.enums.TransactionType;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record TransactionCreateRequest(
        @NotNull(message = "transaction.user.id.null")
        UUID userId,

        @NotNull(message = "transaction.amount.null")
        @Digits(integer = 10, fraction = 2, message = "transaction.amount.digits")
        @Positive(message = "transaction.amount.positive")
        BigDecimal amount,

        @NotNull(message = "transaction.type.null")
        TransactionType type,

        @NotNull(message = "transaction.category.id.null")
        UUID categoryId,

        @NotNull(message = "transaction.account.id.null")
        UUID accountId,

        @Size(max = 500, message = "transaction.description.size")
        String description,

        List<@Size(max = 50, message = "transaction.tag.size") String> tags
) {

}