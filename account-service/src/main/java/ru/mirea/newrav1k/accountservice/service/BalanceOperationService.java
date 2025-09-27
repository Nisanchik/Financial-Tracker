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
import ru.mirea.newrav1k.accountservice.exception.AccountTransferException;
import ru.mirea.newrav1k.accountservice.model.entity.Account;
import ru.mirea.newrav1k.accountservice.model.entity.BankOperation;
import ru.mirea.newrav1k.accountservice.repository.AccountRepository;
import ru.mirea.newrav1k.accountservice.repository.BankOperationRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

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
            @CacheEvict(value = "account-details", key = "#accountId"),
            @CacheEvict(value = "account-details", key = "#trackerId + '-' + #accountId")
    })
    @Transactional
    public void updateBalance(UUID trackerId, UUID accountId, UUID transactionId, BigDecimal amount) {
        log.debug("Update account balance: trackerId={}, accountId={}, transactionId={}", trackerId, accountId, transactionId);
        Account account = findAccountPessimisticByTrackerIdAndIdOrThrow(trackerId, accountId);
        if (isDuplicateOperation(transactionId)) {
            log.info("Skipping update balance: transactionId={}, accountId={}", transactionId, accountId);
            return;
        }
        if (amount.signum() < 0) {
            account.withdraw(amount.abs());
        } else {
            account.deposit(amount);
        }
        this.accountRepository.save(account);
        this.bankOperationRepository.save(new BankOperation(transactionId, accountId, null, amount));
    }

    @PreAuthorize("isAuthenticated()")
    @Caching(evict = {
            @CacheEvict(value = "account-details", key = "#fromAccountId"),
            @CacheEvict(value = "account-details", key = "#toAccountId"),
            @CacheEvict(value = "account-details", key = "#trackerId + '-' + #fromAccountId"),
            @CacheEvict(value = "account-details", key = "#trackerId + '-' + #toAccountId")
    })
    @Transactional
    public void transferFunds(UUID trackerId, UUID fromAccountId, UUID toAccountId, UUID transactionId, BigDecimal amount) {
        log.debug("Transfer funds: trackerId={}, fromAccount={}, toAccount={}, transactionId={}",
                trackerId, fromAccountId, toAccountId, transactionId);
        if (isDuplicateOperation(transactionId)) {
            log.info("Skipping transfer due to duplicate operation: transactionId={}", transactionId);
            return;
        }

        UUID firstAccountId = fromAccountId.compareTo(toAccountId) < 0 ? fromAccountId : toAccountId;
        UUID secondAccountId = fromAccountId.compareTo(toAccountId) < 0 ? toAccountId : fromAccountId;

        Account firstAccount = findAccountPessimisticByTrackerIdAndIdOrThrow(trackerId, firstAccountId);
        Account secondAccount = findAccountPessimisticByTrackerIdAndIdOrThrow(trackerId, secondAccountId);

        if (fromAccountId.equals(toAccountId)) {
            throw new AccountTransferException();
        }

        Account fromAccount = firstAccount.getId().equals(fromAccountId) ? firstAccount : secondAccount;
        Account toAccount = fromAccount == firstAccount ? secondAccount : firstAccount;

        fromAccount.withdraw(amount);
        toAccount.deposit(amount);

        this.accountRepository.saveAll(List.of(fromAccount, toAccount));
        this.bankOperationRepository.save(new BankOperation(transactionId, fromAccountId, toAccountId, amount));
    }

    private Account findAccountPessimisticByTrackerIdAndIdOrThrow(UUID trackerId, UUID accountId) {
        return this.accountRepository.findAccountByTrackerIdAndIdForPessimisticLock(trackerId, accountId)
                .orElseThrow(AccountAccessDeniedException::new);
    }

    private boolean isDuplicateOperation(UUID transactionId) {
        return this.bankOperationRepository.existsByTransactionId(transactionId);
    }

}