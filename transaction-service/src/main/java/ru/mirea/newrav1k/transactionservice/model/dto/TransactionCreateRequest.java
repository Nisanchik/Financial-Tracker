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
        @NotNull(message = "error.transaction_amount_cannot_be_null")
        @Digits(integer = 10, fraction = 2, message = "error.transaction_amount_digits_is_invalid")
        @Positive(message = "error.transaction_amount_is_negative")
        BigDecimal amount,

        @NotNull(message = "error.transaction_type_cannot_be_null")
        TransactionType type,

        @NotNull(message = "error.transaction_category_id_cannot_be_null")
        UUID categoryId,

        // TODO: изменить поле для идентификатора аккаунта отправителя
        @NotNull(message = "error.transaction_account_id_cannot_be_null")
        UUID accountId,

        // TODO: добавить поле для идентификатора аккаунта получателя

        @Size(max = 500, message = "error.transaction_description_size_is_invalid")
        String description,

        List<@Size(max = 50, message = "error.transaction_tag_size_is_invalid") String> tags
) {

}