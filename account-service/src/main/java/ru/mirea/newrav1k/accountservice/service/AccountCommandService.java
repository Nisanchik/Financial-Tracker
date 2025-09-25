package ru.mirea.newrav1k.accountservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mirea.newrav1k.accountservice.exception.AccountAlreadyExistException;
import ru.mirea.newrav1k.accountservice.exception.CreditValidationException;
import ru.mirea.newrav1k.accountservice.mapper.AccountMapper;
import ru.mirea.newrav1k.accountservice.model.dto.AccountCreateRequest;
import ru.mirea.newrav1k.accountservice.model.dto.AccountResponse;
import ru.mirea.newrav1k.accountservice.model.entity.Account;
import ru.mirea.newrav1k.accountservice.model.enums.AccountType;
import ru.mirea.newrav1k.accountservice.repository.AccountRepository;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

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
            noRetryFor = {DataIntegrityViolationException.class},
            backoff = @Backoff(delay = 5000, multiplier = 2),
            maxAttempts = 5
    )
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public AccountResponse createAccount(UUID trackerId, AccountCreateRequest request) {
        log.debug("Creating account: trackerId={}, request={}", trackerId, request);
        Account account = buildAccountFromTrackerIdAndRequest(trackerId, request);
        try {
            Account savedAccount = this.accountRepository.save(account);

            return this.accountMapper.toAccountResponse(savedAccount);
        } catch (DataIntegrityViolationException exception) {
            log.error("Data integrity exception while creation account: trackerId={}, request={}",
                    trackerId, request, exception);
            throw new AccountAlreadyExistException();
        }
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
                    throw new CreditValidationException("Credit limit exceeded");
                }
                return request.creditLimit();
            } else {
                return DEFAULT_CREDIT_AMOUNT;
            }
        }
        return null;
    }

}