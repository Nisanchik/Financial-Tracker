package ru.mirea.newrav1k.transactionservice.configuration.properties;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "transaction-service.kafka.topics")
public record TransactionTopicsProperties(
        @NotBlank String transactionSuccessfullyCreated,
        @NotBlank String transactionBalanceFailure,
        @NotBlank String transactionCompensate,
        @NotBlank String transactionCompensateFailure,
        @NotBlank String transactionCompensateDifferenceAmount
) {

}