package ru.mirea.newrav1k.transactionservice.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.mirea.newrav1k.transactionservice.model.entity.Transaction;
import ru.mirea.newrav1k.transactionservice.model.enums.TransactionType;
import ru.mirea.newrav1k.transactionservice.service.client.AccountClient;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BalanceService {

    private final AccountClient accountClient;

    @CircuitBreaker(name = "balanceService", fallbackMethod = "updateBalanceFallback")
    public void updateBalance(UUID accountId, TransactionType transactionType, BigDecimal amount) {
        log.debug("Updating balance for account {}", accountId);
        BigDecimal updateAmount = transactionType.equals(TransactionType.INCOME) ? amount : amount.negate();
        this.accountClient.updateBalance(accountId, updateAmount);
        log.debug("Successfully updated account balance for account {}", accountId);
    }

    private void updateBalanceFallback(UUID accountId, TransactionType transactionType, BigDecimal amount, Exception exception) {
        log.error("Could not update account balance for account {} (type={}, amount={})",
                accountId, transactionType, amount, exception);
        // TODO: добавить сохранение транзакции для будущей обработки
    }

    @CircuitBreaker(name = "balanceService", fallbackMethod = "compensateTransactionFallback")
    public void compensateTransaction(Transaction transaction) {
        log.debug("Starting compensation of transaction {}", transaction);
        BigDecimal compensationAmount = transaction.getType() == TransactionType.INCOME
                ? transaction.getAmount().negate()
                : transaction.getAmount();
        log.debug("Successfully compensated transaction for account {}", transaction.getAccountId());
        this.accountClient.updateBalance(transaction.getAccountId(), compensationAmount);
    }

    private void compensateTransactionFallback(Transaction transaction, Exception exception) {
        log.error("Could not compensate transaction for account {}", transaction.getAccountId(), exception);
        // TODO: добавить сохранение транзакции для будущей обработки
    }

}