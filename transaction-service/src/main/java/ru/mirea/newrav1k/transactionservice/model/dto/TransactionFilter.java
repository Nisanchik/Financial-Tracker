package ru.mirea.newrav1k.transactionservice.model.dto;

import org.springframework.format.annotation.DateTimeFormat;

import java.time.Instant;

public record TransactionFilter(
        String type,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant createdAt
) {

}