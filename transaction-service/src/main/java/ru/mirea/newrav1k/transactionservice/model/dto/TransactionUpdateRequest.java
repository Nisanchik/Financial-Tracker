package ru.mirea.newrav1k.transactionservice.model.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record TransactionUpdateRequest(
        @Digits(integer = 10, fraction = 2, message = "error.transaction_amount_digits_is_invalid")
        @Positive(message = "error.transaction_amount_is_negative")
        BigDecimal amount,

        @Size(max = 500, message = "error.transaction_description_size_is_invalid")
        String description
) {

}