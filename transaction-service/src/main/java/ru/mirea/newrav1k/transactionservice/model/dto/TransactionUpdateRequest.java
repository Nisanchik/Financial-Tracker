package ru.mirea.newrav1k.transactionservice.model.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record TransactionUpdateRequest(
        @Digits(integer = 10, fraction = 2, message = "transaction.amount.digits")
        @Positive(message = "transaction.amount.positive")
        BigDecimal amount,

        @Size(max = 500, message = "transaction.description.size")
        String description
) {

}