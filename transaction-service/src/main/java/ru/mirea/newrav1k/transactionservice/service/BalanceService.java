package ru.mirea.newrav1k.transactionservice.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.mirea.newrav1k.transactionservice.event.publisher.TransactionEventPublisher;
import ru.mirea.newrav1k.transactionservice.model.enums.TransactionType;
import ru.mirea.newrav1k.transactionservice.service.client.AccountClient;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BalanceService {

    private final TransactionEventPublisher transactionEventPublisher;

    private final AccountClient accountClient;

    @CircuitBreaker(name = "updateBalance", fallbackMethod = "updateBalanceFallback")
    public void updateBalance(UUID transactionId, UUID accountId, TransactionType transactionType, BigDecimal amount) {
        log.debug("Updating balance for account {} from transaction {}", accountId, transactionId);
        BigDecimal updateAmount = transactionType.equals(TransactionType.INCOME) ? amount : amount.negate();
        this.accountClient.updateBalance(accountId, transactionId, updateAmount);
        log.debug("Successfully updated account balance for account {}", accountId);
    }

    private void updateBalanceFallback(UUID transactionId, UUID accountId,
                                       TransactionType transactionType, BigDecimal amount, Throwable exception) {
        log.error("Error updating balance for account {} from transaction {}", accountId, transactionId, exception);
        this.transactionEventPublisher.publishBalanceUpdateFailureEvent(transactionId, accountId, transactionType, amount);
    }

    @CircuitBreaker(name = "compensateTransaction", fallbackMethod = "compensateTransactionFallback")
    public void compensateTransaction(UUID transactionId, UUID accountId, TransactionType transactionType, BigDecimal amount) {
        log.debug("Starting to compensate transaction for account {}", accountId);
        BigDecimal compensationAmount = transactionType == TransactionType.INCOME
                ? amount.negate()
                : amount;
        this.accountClient.updateBalance(accountId, transactionId, compensationAmount);
        log.debug("Successfully compensated transaction for account {}", accountId);
    }

    private void compensateTransactionFallback(UUID transactionId, UUID accountId,
                                               TransactionType transactionType, BigDecimal amount, Throwable exception) {
        log.error("Cannot compensate transaction {} for account {}", transactionId, accountId, exception);
        this.transactionEventPublisher.publishTransactionCompensateEvent(transactionId, accountId, transactionType, amount);
    }

    @CircuitBreaker(name = "compensateDifferenceAmount", fallbackMethod = "compensateDifferenceAmountFallback")
    public void compensateDifferenceAmount(UUID transactionId, UUID accountId,
                                           TransactionType transactionType, BigDecimal oldAmount, BigDecimal newAmount) {
        log.debug("Starting to compensate difference amount for account {}", accountId);
        BigDecimal compensationAmount = newAmount.subtract(oldAmount);
        if (transactionType.equals(TransactionType.EXPENSE)) {
            compensationAmount = compensationAmount.negate();
        }
        this.accountClient.updateBalance(accountId, transactionId, compensationAmount);
        log.debug("Successfully compensated difference amount for account {}", accountId);
    }

    private void compensateDifferenceAmountFallback(UUID transactionId, UUID accountId, TransactionType transactionType,
                                                    BigDecimal oldAmount, BigDecimal newAmount, Throwable exception) {
        log.error("Cannot compensate difference amount for account {} with transaction type {}", accountId, transactionType, exception);
        this.transactionEventPublisher.publishTransactionCompensateDifferenceAmountEvent(
                transactionId,
                accountId,
                transactionType,
                oldAmount,
                newAmount
        );
    }

}