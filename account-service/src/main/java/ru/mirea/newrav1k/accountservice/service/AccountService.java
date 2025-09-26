package ru.mirea.newrav1k.accountservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
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
import ru.mirea.newrav1k.accountservice.exception.AccountDuplicateException;
import ru.mirea.newrav1k.accountservice.exception.AccountNotFoundException;
import ru.mirea.newrav1k.accountservice.exception.AccountServiceException;
import ru.mirea.newrav1k.accountservice.exception.AccountTransferException;
import ru.mirea.newrav1k.accountservice.exception.AccountValidationException;
import ru.mirea.newrav1k.accountservice.mapper.AccountMapper;
import ru.mirea.newrav1k.accountservice.model.dto.AccountCreateRequest;
import ru.mirea.newrav1k.accountservice.model.dto.AccountFilter;
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

import static ru.mirea.newrav1k.accountservice.utils.MessageCode.ACCOUNT_INACTIVE;
import static ru.mirea.newrav1k.accountservice.utils.MessageCode.ACCOUNT_NAME_ALREADY_EXIST;
import static ru.mirea.newrav1k.accountservice.utils.MessageCode.ACCOUNT_TYPE_CANNOT_UPDATE;

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
        // TODO: фильтрация списка пользователей для администратора
        return this.accountRepository.findAll(pageable)
                .map(this.accountMapper::toAccountResponse);
    }

    @PreAuthorize("isAuthenticated()")
    public Page<AccountResponse> findAllAccountsByTrackerId(UUID trackerId, AccountFilter filter, Pageable pageable) {
        log.debug("Finding all tracker accounts");
        AccountFilter updatedFilter = new AccountFilter(trackerId, filter.name(), filter.currency(), filter.createdAtFrom(), filter.createdAtTo());
        Specification<Account> specification = this.accountRepository.buildAccountSpecification(updatedFilter);
        return this.accountRepository.findAll(specification, pageable)
                .map(this.accountMapper::toAccountResponse);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Cacheable(value = "account-details", key = "#accountId")
    public AccountResponse findById(UUID accountId) {
        log.debug("Finding account: accountId={}", accountId);
        return this.accountRepository.findById(accountId)
                .map(this.accountMapper::toAccountResponse)
                .orElseThrow(AccountNotFoundException::new);
    }

    @PreAuthorize("isAuthenticated()")
    @Cacheable(value = "account-details", key = "#trackerId + '-' + #accountId")
    public AccountResponse findByTrackerIdAndAccountId(UUID trackerId, UUID accountId) {
        log.debug("Finding account: trackerId={}, accountId={}", trackerId, accountId);
        return this.accountRepository.findAccountByTrackerIdAndId(trackerId, accountId)
                .map(this.accountMapper::toAccountResponse)
                .orElseThrow(AccountAccessDeniedException::new);
    }

    @Retryable(
            retryFor = {TransientDataAccessException.class},
            backoff = @Backoff(delay = 5000, multiplier = 2),
            noRetryFor = {DataIntegrityViolationException.class},
            maxAttempts = 5
    )
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public AccountResponse create(AccountCreateRequest request, UUID trackerId) {
        log.debug("Creating account: request={}, trackerId={}", request, trackerId);
        Account account = buildAccountFromRequestAndTrackerId(request, trackerId);
        try {
            this.accountRepository.save(account);

            return this.accountMapper.toAccountResponse(account);
        } catch (DataIntegrityViolationException exception) {
            log.error("Account already exist", exception);
            throw new AccountDuplicateException(ACCOUNT_NAME_ALREADY_EXIST);
        }
    }

    @PreAuthorize("isAuthenticated()")
    @Caching(evict = {
            @CacheEvict(value = "account-details", key = "#trackerId + '-' + #accountId")
    })
    @Transactional
    public AccountResponse update(UUID trackerId, UUID accountId, AccountUpdateRequest request) {
        log.debug("Update account: trackerId={}, accountId={}, request={}", trackerId, accountId, request);
        return this.accountRepository.findAccountByTrackerIdAndId(trackerId, accountId)
                .map(account -> {
                    account.setName(request.name());
                    if (request.currency() != null && account.getBalance().signum() == 0) {
                        account.setCurrency(request.currency());
                    }
                    return this.accountRepository.save(account);
                })
                .map(this.accountMapper::toAccountResponse)
                .orElseThrow(AccountAccessDeniedException::new);
    }

    @PreAuthorize("isAuthenticated()")
    @Caching(evict = {
            @CacheEvict(value = "account-details", key = "#trackerId + '-' + #accountId")
    })
    @Transactional
    public AccountResponse update(UUID trackerId, UUID accountId, JsonNode jsonNode) {
        log.debug("Update account: trackerId={}, accountId={}, jsonNode={}", trackerId, accountId, jsonNode);
        Account account = findAccountByTrackerIdAndIdOrThrow(trackerId, accountId);
        try {
            if (jsonNode.has("name")) {
                account.setName(jsonNode.get("name").asText());
            }
            if (jsonNode.has("currency") && account.getBalance().signum() == 0) {
                Currency currency = Currency.findCurrency(jsonNode.get("currency").asText());
                account.setCurrency(currency);
            }
            if (jsonNode.has("type")) {
                throw new AccountValidationException(ACCOUNT_TYPE_CANNOT_UPDATE);
            }
            // TODO: проверку на изменение кредита
            return this.accountMapper.toAccountResponse(account);
        } catch (Exception exception) {
            log.error("Cannot update account: trackerId={}, jsonNode={}", trackerId, jsonNode, exception);
            throw new AccountServiceException(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PreAuthorize("isAuthenticated()")
    @Caching(evict = {
            @CacheEvict(value = "account-details", key = "#trackerId + '-' + #accountId")
    })
    @Transactional
    public void softDeleteById(UUID trackerId, UUID accountId) {
        log.debug("Soft delete account: trackerId={}, accountId={}", trackerId, accountId);
        Account account = findAccountByTrackerIdAndIdOrThrow(trackerId, accountId);
        validateAccountActive(account);
        account.softDelete();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Caching(evict = {
            @CacheEvict(value = "account-details", key = "'*-' + #accountId")
    })
    @Transactional
    public void hardDeleteById(UUID accountId) {
        log.debug("Hard delete account: accountId={}", accountId);
        this.accountRepository.deleteById(accountId);
    }

    @PreAuthorize("isAuthenticated()")
    @Caching(evict = {
            @CacheEvict(value = "account-details", key = "#trackerId + '-' + #accountId")
    })
    @Retryable(
            retryFor = {
                    ObjectOptimisticLockingFailureException.class,
                    OptimisticLockingFailureException.class,
                    DataIntegrityViolationException.class
            }, backoff = @Backoff(value = 300, maxDelay = 2000, multiplier = 2),
            maxAttempts = 5
    )
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateBalance(UUID trackerId, UUID accountId, UUID transactionId, BigDecimal amount) {
        log.debug("Update account balance: trackerId={}, accountId={}, transactionId={}", trackerId, accountId, transactionId);
        if (this.bankOperationRepository.existsByTransactionId(transactionId)) {
            log.warn("Account was updated from transaction with id {}", transactionId);
            return;
        }
        this.bankOperationRepository.save(new BankOperation(transactionId, accountId, amount));
        Account account = this.accountRepository.findAccountByTrackerIdAndIdForPessimisticLock(trackerId, accountId)
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
    @Caching(evict = {
            @CacheEvict(value = "account-details", key = "#trackerId + '-' + #accountId")
    })
    @Transactional
    public void depositMoney(UUID trackerId, UUID accountId, BigDecimal amount) {
        log.debug("Deposit money: accountId={}, amount={}", accountId, amount);
        Account account = findAccountByTrackerIdAndIdOrThrow(trackerId, accountId);
        account.deposit(amount);
    }

    @Deprecated(forRemoval = true)
    @PreAuthorize("isAuthenticated()")
    @Caching(evict = {
            @CacheEvict(value = "account-details", key = "#trackerId + '-' + #accountId")
    })
    @Transactional
    public void withdrawMoney(UUID trackerId, UUID accountId, BigDecimal amount) {
        log.debug("Withdraw money: accountId={}, amount={}", accountId, amount);
        Account account = findAccountByTrackerIdAndIdOrThrow(trackerId, accountId);
        account.withdraw(amount);
    }

    @PreAuthorize("isAuthenticated()")
    @Caching(evict = {
            @CacheEvict(value = "account-details", key = "#trackerId + '-' + #fromAccountId"),
            @CacheEvict(value = "account-details", key = "#trackerId + '-' + #toAccountId")
    })
    @Transactional
    public void transferMoney(UUID trackerId, UUID fromAccountId, UUID toAccountId, BigDecimal amount) {
        log.debug("Transfer money to other account: fromAccountId={}, toAccountId={}", fromAccountId, toAccountId);
        if (fromAccountId.equals(toAccountId)) {
            throw new AccountTransferException();
        }
        Account fromAccount = findAccountByTrackerIdAndIdOrThrow(trackerId, fromAccountId);
        Account toAccount = findAccountByTrackerIdAndIdOrThrow(trackerId, toAccountId);
        if (!fromAccount.getCurrency().equals(toAccount.getCurrency())) {
            log.warn("Transfer money from currency does not match to currency");
            // TODO: реализовать конвертацию и корректную транзакцию между счетами
            return;
        }
        fromAccount.withdraw(amount);
        toAccount.deposit(amount);
        this.accountRepository.save(fromAccount);
    }

    @PreAuthorize("isAuthenticated()")
    @Caching(evict = {
            @CacheEvict(value = "account-details", key = "#trackerId + '-' + #accountId")
    })
    @Transactional
    public void deactivateAccount(UUID trackerId, UUID accountId) {
        log.debug("Deactivate account: trackerId={}, accountId={}", trackerId, accountId);
        Account account = findAccountByTrackerIdAndIdOrThrow(trackerId, accountId);
        account.deactivate();
    }

    private Account buildAccountFromRequestAndTrackerId(AccountCreateRequest request, UUID trackerId) {
        Account account = new Account();

        account.setTrackerId(trackerId);
        account.setName(request.name());
        account.setCurrency(request.currency());
        account.setType(request.type());

        account.setBalance(BigDecimal.ZERO);
        account.setActive(true);

        return account;
    }

    private Account findAccountByTrackerIdAndIdOrThrow(UUID trackerId, UUID accountId) {
        return this.accountRepository.findAccountByTrackerIdAndId(trackerId, accountId)
                .orElseThrow(AccountAccessDeniedException::new);
    }

    private void validateAccountActive(Account account) {
        if (!account.isActive()) {
            log.warn("Account is not active: accountId={}", account.getId());
            throw new AccountValidationException(ACCOUNT_INACTIVE);
        }
    }

    @Recover
    private void updateBalanceRecover(ObjectOptimisticLockingFailureException ex,
                                      UUID trackerId, UUID accountId, UUID transactionId, BigDecimal amount) {
        log.error("Error while update balance: " +
                "trackerId={}, accountId={}, transactionId={}, amount={}", trackerId, accountId, transactionId, amount, ex);
        throw new AccountBalanceException(MessageCode.UPDATE_BALANCE_FAILED);
    }

}