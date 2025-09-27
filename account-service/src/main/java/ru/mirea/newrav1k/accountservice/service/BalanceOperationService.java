package ru.mirea.newrav1k.accountservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mirea.newrav1k.accountservice.exception.AccountAccessDeniedException;
import ru.mirea.newrav1k.accountservice.exception.AccountValidationException;
import ru.mirea.newrav1k.accountservice.model.entity.Account;
import ru.mirea.newrav1k.accountservice.model.entity.BankOperation;
import ru.mirea.newrav1k.accountservice.repository.AccountRepository;
import ru.mirea.newrav1k.accountservice.repository.BankOperationRepository;

import java.math.BigDecimal;
import java.util.UUID;

import static ru.mirea.newrav1k.accountservice.utils.MessageCode.ACCOUNT_INACTIVE;

@Slf4j
@Service
@RequiredArgsConstructor
public class BalanceOperationService {

    private final BankOperationRepository bankOperationRepository;

    private final AccountRepository accountRepository;

    @PreAuthorize("isAuthenticated()")
    @Retryable(
            retryFor = {
                    ObjectOptimisticLockingFailureException.class,
                    OptimisticLockingFailureException.class,
                    DataIntegrityViolationException.class
            }, backoff = @Backoff(value = 300, maxDelay = 2000, multiplier = 2),
            maxAttempts = 5
    )
    @Caching(evict = {
            @CacheEvict(value = "account-details", key = "#trackerId + '-' + #accountId")
    })
    @Transactional
    public void updateBalance(UUID trackerId, UUID accountId, UUID transactionId, BigDecimal amount) {
        log.debug("Update account balance: trackerId={}, accountId={}, transactionId={}", trackerId, accountId, transactionId);
        Account account = findAccountPessimisticByTrackerIdAndIdOrThrow(trackerId, accountId);
        if (this.bankOperationRepository.existsByTransactionId(transactionId)) {
            log.warn("Account was updated from transaction with id {}", transactionId);
            return;
        }
        validateAccountActive(account);
        if (amount.signum() < 0) {
            account.withdraw(amount.abs());
        } else {
            account.deposit(amount);
        }
        this.accountRepository.save(account);
        this.bankOperationRepository.save(new BankOperation(transactionId, accountId, amount));
    }

    private Account findAccountPessimisticByTrackerIdAndIdOrThrow(UUID trackerId, UUID accountId) {
        return this.accountRepository.findAccountByTrackerIdAndIdForPessimisticLock(trackerId, accountId)
                .orElseThrow(AccountAccessDeniedException::new);
    }

    private void validateAccountActive(Account account) {
        if (!account.isActive()) {
            log.warn("Account is not active: accountId={}", account.getId());
            throw new AccountValidationException(ACCOUNT_INACTIVE);
        }
    }

}