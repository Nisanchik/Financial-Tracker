package org.example.transactionservice.model.dto;

import java.math.BigDecimal;

public record TransactionUpdateRequest(
        BigDecimal amount,
        String description

) {

}