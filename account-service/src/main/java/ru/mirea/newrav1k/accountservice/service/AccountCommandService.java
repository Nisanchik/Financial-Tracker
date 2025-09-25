package ru.mirea.newrav1k.accountservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mirea.newrav1k.accountservice.exception.AccountAccessDeniedException;
import ru.mirea.newrav1k.accountservice.exception.AccountDuplicateException;
import ru.mirea.newrav1k.accountservice.exception.AccountServiceException;
import ru.mirea.newrav1k.accountservice.exception.AccountValidationException;
import ru.mirea.newrav1k.accountservice.mapper.AccountMapper;
import ru.mirea.newrav1k.accountservice.model.dto.AccountCreateRequest;
import ru.mirea.newrav1k.accountservice.model.dto.AccountResponse;
import ru.mirea.newrav1k.accountservice.model.dto.AccountUpdateRequest;
import ru.mirea.newrav1k.accountservice.model.entity.Account;
import ru.mirea.newrav1k.accountservice.model.enums.AccountType;
import ru.mirea.newrav1k.accountservice.model.enums.Currency;
import ru.mirea.newrav1k.accountservice.repository.AccountRepository;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

import static ru.mirea.newrav1k.accountservice.utils.MessageCode.ACCOUNT_CREDIT_LIMIT_IS_INSUFFICIENT;
import static ru.mirea.newrav1k.accountservice.utils.MessageCode.ACCOUNT_CURRENCY_CANNOT_UPDATE;
import static ru.mirea.newrav1k.accountservice.utils.MessageCode.ACCOUNT_INACTIVE;
import static ru.mirea.newrav1k.accountservice.utils.MessageCode.ACCOUNT_NAME_ALREADY_EXIST;
import static ru.mirea.newrav1k.accountservice.utils.MessageCode.ACCOUNT_TYPE_CANNOT_UPDATE;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountCommandService {

    private static final BigDecimal DEFAULT_CREDIT_AMOUNT = BigDecimal.valueOf(10_000L);

    private static final BigDecimal MAX_CREDIT_AMOUNT = BigDecimal.valueOf(200_000L);

    private final AccountRepository accountRepository;

    private final AccountMapper accountMapper;

    @Retryable(
            retryFor = {TransientDataAccessException.class},
            noRetryFor = {
                    DataIntegrityViolationException.class,
                    AccountDuplicateException.class,
            },
            backoff = @Backoff(delay = 5000, multiplier = 2),
            maxAttempts = 5
    )
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public AccountResponse createAccount(UUID trackerId, AccountCreateRequest request) {
        log.debug("Creating account: trackerId={}, request={}", trackerId, request);
        Account account = buildAccountFromTrackerIdAndRequest(trackerId, request);
        if (this.accountRepository.existsByTrackerIdAndName(trackerId, request.name())) {
            throw new AccountValidationException(ACCOUNT_NAME_ALREADY_EXIST);
        }
        try {
            Account savedAccount = this.accountRepository.save(account);

            this.accountRepository.flush();

            return this.accountMapper.toAccountResponse(savedAccount);
        } catch (DataIntegrityViolationException exception) {
            log.error("Data integrity exception while creation account: trackerId={}, request={}",
                    trackerId, request, exception);
            throw new AccountDuplicateException(ACCOUNT_NAME_ALREADY_EXIST);
        }
    }

    @PreAuthorize("isAuthenticated()")
    @Caching(evict = {
            @CacheEvict(value = "account-details", key = "#trackerId + '-' + #accountId")
    })
    @Transactional
    public AccountResponse updateAccount(UUID trackerId, UUID accountId, AccountUpdateRequest request) {
        log.debug("Update account: trackerId={}, accountId={}, request={}", trackerId, accountId, request);
        Account account = findAccountByTrackerIdAndIdOrThrow(trackerId, accountId);

        if (this.accountRepository.existsByTrackerIdAndName(trackerId, request.name())) {
            throw new AccountValidationException(ACCOUNT_NAME_ALREADY_EXIST);
        }

        if (request.currency() == null && account.getBalance().signum() != 0) {
            throw new AccountValidationException(ACCOUNT_CURRENCY_CANNOT_UPDATE);
        }

        account.setName(request.name());
        account.setCurrency(request.currency());

        Account savedAccount = accountRepository.save(account);
        return this.accountMapper.toAccountResponse(savedAccount);
    }

    @PreAuthorize("isAuthenticated()")
    @Caching(evict = {
            @CacheEvict(value = "account-details", key = "#trackerId + '-' + #accountId")
    })
    @Transactional
    public AccountResponse patchAccount(UUID trackerId, UUID accountId, JsonNode jsonNode) {
        log.debug("Patch account: trackerId={}, accountId={}, jsonNode={}", trackerId, accountId, jsonNode);
        Account account = findAccountByTrackerIdAndIdOrThrow(trackerId, accountId);
        try {
            if (jsonNode.has("name")) {
                account.setName(jsonNode.get("name").asText());
            }
            if (jsonNode.has("currency")) {
                if (account.getBalance().signum() != 0) {
                    throw new AccountValidationException(ACCOUNT_CURRENCY_CANNOT_UPDATE);
                }
                Currency currency = Currency.findCurrency(jsonNode.get("currency").asText());
                account.setCurrency(currency);
            }
            if (jsonNode.has("type")) {
                throw new AccountValidationException(ACCOUNT_TYPE_CANNOT_UPDATE);
            }
            Account savedAccount = this.accountRepository.save(account);
            return this.accountMapper.toAccountResponse(savedAccount);
        } catch (AccountValidationException exception) {
            throw exception;
        } catch (Exception exception) {
            log.error("Error while updating account: trackerId={}, accountId={}", trackerId, accountId, exception);
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
            @CacheEvict(value = "account-details", key = "'*-' + #accountId"),
            @CacheEvict(value = "account-details", key = "#accountId")
    })
    @Transactional
    public void hardDeleteById(UUID accountId) {
        log.debug("Hard delete account: accountId={}", accountId);
        this.accountRepository.deleteById(accountId);
    }

    private Account findAccountByTrackerIdAndIdOrThrow(UUID trackerId, UUID accountId) {
        return this.accountRepository.findAccountByTrackerIdAndId(trackerId, accountId)
                .orElseThrow(AccountAccessDeniedException::new);
    }

    private Account buildAccountFromTrackerIdAndRequest(UUID trackerId, AccountCreateRequest request) {
        Account account = new Account();

        account.setTrackerId(trackerId);
        account.setName(request.name());
        account.setCurrency(request.currency());

        account.setCreditLimit(determineCreditLimitByRequest(request));
        account.setType(request.type());

        account.setBalance(BigDecimal.ZERO);
        account.setActive(true);

        return account;
    }

    private BigDecimal determineCreditLimitByRequest(AccountCreateRequest request) {
        if (request.type() == AccountType.CREDIT_CARD) {
            if (Objects.nonNull(request.creditLimit())) {
                if (request.creditLimit().compareTo(MAX_CREDIT_AMOUNT) > 0) {
                    log.warn("Credit limit exceeded, request={}", request);
                    throw new AccountValidationException(ACCOUNT_CREDIT_LIMIT_IS_INSUFFICIENT);
                }
                return request.creditLimit();
            } else {
                return DEFAULT_CREDIT_AMOUNT;
            }
        }
        return null;
    }

    private void validateAccountActive(Account account) {
        if (!account.isActive()) {
            log.warn("Account is not active: accountId={}", account.getId());
            throw new AccountValidationException(ACCOUNT_INACTIVE);
        }
    }

}