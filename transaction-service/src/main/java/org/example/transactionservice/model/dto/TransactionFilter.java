package org.example.transactionservice.model.dto;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.Instant;

public record TransactionFilter(
        String type,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdAt
) {

}