package ru.mirea.newrav1k.accountservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.mirea.newrav1k.accountservice.exception.AccountAccessDeniedException;
import ru.mirea.newrav1k.accountservice.exception.AccountBalanceException;
import ru.mirea.newrav1k.accountservice.exception.AccountNotFoundException;
import ru.mirea.newrav1k.accountservice.exception.AccountServiceException;
import ru.mirea.newrav1k.accountservice.exception.AccountStateException;
import ru.mirea.newrav1k.accountservice.exception.AccountTypeException;
import ru.mirea.newrav1k.accountservice.mapper.AccountMapper;
import ru.mirea.newrav1k.accountservice.model.dto.AccountCreateRequest;
import ru.mirea.newrav1k.accountservice.model.dto.AccountResponse;
import ru.mirea.newrav1k.accountservice.model.dto.AccountUpdateRequest;
import ru.mirea.newrav1k.accountservice.model.entity.Account;
import ru.mirea.newrav1k.accountservice.model.entity.BankOperation;
import ru.mirea.newrav1k.accountservice.model.enums.Currency;
import ru.mirea.newrav1k.accountservice.repository.AccountRepository;
import ru.mirea.newrav1k.accountservice.repository.BankOperationRepository;
import ru.mirea.newrav1k.accountservice.utils.MessageCode;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountService {

    private final BankOperationRepository bankOperationRepository;

    private final AccountRepository accountRepository;

    private final AccountMapper accountMapper;

    @PreAuthorize("hasRole('ADMIN')")
    public Page<AccountResponse> findAll(Pageable pageable) {
        log.debug("Finding all accounts");
        return this.accountRepository.findAll(pageable)
                .map(this.accountMapper::toAccountResponse);
    }

    public Page<AccountResponse> findAllAccountsByUserId(UUID userId, Pageable pageable) {
        log.debug("Finding all user accounts");
        return this.accountRepository.findAllByUserId(userId, pageable)
                .map(this.accountMapper::toAccountResponse);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public AccountResponse findById(UUID accountId) {
        log.debug("Finding account: accountId={}", accountId);
        return this.accountRepository.findById(accountId)
                .map(this.accountMapper::toAccountResponse)
                .orElseThrow(AccountNotFoundException::new);
    }

    public AccountResponse findByUserIdAndAccountId(UUID userId, UUID accountId) {
        log.debug("Finding account: userId={}, accountId={}", userId, accountId);
        return this.accountRepository.findAccountByUserIdAndId(userId, accountId)
                .map(this.accountMapper::toAccountResponse)
                .orElseThrow(AccountAccessDeniedException::new);
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional
    public AccountResponse create(AccountCreateRequest request, UUID userId) {
        log.debug("Creating account: request={}, userId={}", request, userId);
        Account account = buildAccountFromRequestAndUserId(request, userId);
        this.accountRepository.save(account);
        return this.accountMapper.toAccountResponse(account);
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional
    public AccountResponse update(UUID userId, UUID accountId, AccountUpdateRequest request) {
        log.debug("Update account: userId={}, accountId={}, request={}", userId, accountId, request);
        return this.accountRepository.findAccountByUserIdAndId(userId, accountId)
                .map(account -> {
                    account.setName(request.name());
                    if (request.currency() != null) {
                        account.setCurrency(request.currency());
                        // TODO: изменение курса (опционально)
                    }
                    return account;
                })
                .map(this.accountMapper::toAccountResponse)
                .orElseThrow(AccountAccessDeniedException::new);
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional
    public AccountResponse update(UUID userId, UUID accountId, JsonNode jsonNode) {
        log.debug("Update account: userId={}, accountId={}, jsonNode={}", userId, accountId, jsonNode);
        Account account = findAccountByUserIdAndIdOrThrow(userId, accountId);
        try {
            if (jsonNode.has("name")) {
                account.setName(jsonNode.get("name").asText());
            }
            if (jsonNode.has("currency")) {
                Currency currency = Currency.findCurrency(jsonNode.get("currency").asText());
                account.setCurrency(currency);
                // TODO: изменение курса (опционально)
            }
            if (jsonNode.has("type")) {
                throw new AccountTypeException();
            }
            return this.accountMapper.toAccountResponse(account);
        } catch (Exception exception) {
            throw new AccountServiceException("Error while updating account");
        }
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional
    public void deleteById(UUID userId, UUID accountId) {
        log.debug("Deleting account: userId={}, accountId={}", userId, accountId);
        this.accountRepository.deleteAccountByUserIdAndId(userId, accountId);
    }

    @PreAuthorize("isAuthenticated()")
    @Retryable(
            retryFor = {
                    ObjectOptimisticLockingFailureException.class,
                    OptimisticLockingFailureException.class,
                    DataIntegrityViolationException.class
            }, backoff = @Backoff(value = 300, maxDelay = 2000, multiplier = 2),
            maxAttempts = 5
    )
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateBalance(UUID userId, UUID accountId, UUID transactionId, BigDecimal amount) {
        log.debug("Update account balance: userId={}, accountId={}, transactionId={}", userId, accountId, transactionId);
        if (this.bankOperationRepository.existsByTransactionId(transactionId)) {
            log.warn("Account was updated from transaction with id {}", transactionId);
            return;
        }
        this.bankOperationRepository.save(new BankOperation(transactionId, accountId, amount));
        Account account = this.accountRepository.findAccountByUserIdAndIdForPessimisticLock(userId, accountId)
                .orElseThrow(AccountAccessDeniedException::new);
        validateAccountActive(account);
        if (amount.signum() < 0) {
            account.withdraw(amount.abs());
        } else {
            account.deposit(amount);
        }
        this.accountRepository.save(account);
    }

    @Deprecated(forRemoval = true)
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public void depositMoney(UUID userId, UUID accountId, BigDecimal amount) {
        log.debug("Deposit money: accountId={}, amount={}", accountId, amount);
        Account account = findAccountByUserIdAndIdOrThrow(userId, accountId);
        account.deposit(amount);
    }

    @Deprecated(forRemoval = true)
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public void withdrawMoney(UUID userId, UUID accountId, BigDecimal amount) {
        log.debug("Withdraw money: accountId={}, amount={}", accountId, amount);
        Account account = findAccountByUserIdAndIdOrThrow(userId, accountId);
        account.withdraw(amount);
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional
    public void deactivateAccount(UUID userId, UUID accountId) {
        log.debug("Deactivate account: userId={}, accountId={}", userId, accountId);
        Account account = findAccountByUserIdAndIdOrThrow(userId, accountId);
        account.deactivate();
    }

    private Account buildAccountFromRequestAndUserId(AccountCreateRequest request, UUID userId) {
        Account account = new Account();

        account.setUserId(userId);
        account.setName(request.name());
        account.setCurrency(request.currency());
        account.setType(request.type());

        account.setBalance(BigDecimal.ZERO);
        account.setActive(true);

        return account;
    }

    private Account findAccountByUserIdAndIdOrThrow(UUID userId, UUID accountId) {
        return this.accountRepository.findAccountByUserIdAndId(userId, accountId)
                .orElseThrow(AccountAccessDeniedException::new);
    }

    private void validateAccountActive(Account account) {
        if (!account.isActive()) {
            log.warn("Account is not active: accountId={}", account.getId());
            throw new AccountStateException();
        }
    }

    @Recover
    public void updateBalanceRecover(ObjectOptimisticLockingFailureException ex,
                                     UUID userId, UUID accountId, UUID transactionId, BigDecimal amount) {
        log.error("Error while update balance: " +
                "userId={}, accountId={}, transactionId={}, amount={}", userId, accountId, transactionId, amount, ex);
        throw new AccountBalanceException(MessageCode.UPDATE_BALANCE_FAILED);
    }

}