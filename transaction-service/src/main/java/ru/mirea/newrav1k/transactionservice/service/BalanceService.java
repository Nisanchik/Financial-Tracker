package ru.mirea.newrav1k.transactionservice.service;

import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.mirea.newrav1k.transactionservice.event.publisher.TransactionEventPublisher;
import ru.mirea.newrav1k.transactionservice.exception.TransactionServiceException;
import ru.mirea.newrav1k.transactionservice.model.enums.TransactionType;
import ru.mirea.newrav1k.transactionservice.service.client.AccountClient;

import java.math.BigDecimal;
import java.util.UUID;

import static ru.mirea.newrav1k.transactionservice.utils.MessageCode.TRANSACTION_COMPENSATE_FAILED;
import static ru.mirea.newrav1k.transactionservice.utils.MessageCode.TRANSACTION_CREATE_FAILED;

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
                                       TransactionType transactionType, BigDecimal amount, Throwable throwable) {
        log.error("Error updating balance for account {} from transaction {}", accountId, transactionId, throwable);
        this.transactionEventPublisher.publishExternalBalanceUpdateFailureEvent(transactionId, accountId, transactionType, amount);
        handleThrowableWithServiceUnavailable(TRANSACTION_CREATE_FAILED, throwable);
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
                                               TransactionType transactionType, BigDecimal amount, Throwable throwable) {
        log.error("Cannot compensate transaction {} for account {}", transactionId, accountId, throwable);
        this.transactionEventPublisher.publishExternalTransactionCompensateEvent(transactionId, accountId, transactionType, amount);
        handleThrowableWithServiceUnavailable(TRANSACTION_COMPENSATE_FAILED, throwable);
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
                                                    BigDecimal oldAmount, BigDecimal newAmount, Throwable throwable) {
        log.error("Cannot compensate difference amount for account {} with transaction type {}", accountId, transactionType, throwable);
        this.transactionEventPublisher.publishExternalCompensateDifferenceAmountEvent(
                transactionId,
                accountId,
                transactionType,
                oldAmount,
                newAmount
        );
        handleThrowableWithServiceUnavailable(TRANSACTION_COMPENSATE_FAILED, throwable);
    }

    private void handleThrowableWithServiceUnavailable(String message, Throwable throwable) {
        if (throwable instanceof FeignException feignException) {
            throw feignException;
        }
        throw new TransactionServiceException(message, HttpStatus.SERVICE_UNAVAILABLE);
    }

}