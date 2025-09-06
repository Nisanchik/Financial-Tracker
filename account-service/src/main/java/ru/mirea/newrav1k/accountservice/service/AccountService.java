package ru.mirea.newrav1k.accountservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.mirea.newrav1k.accountservice.exception.AccountBalanceException;
import ru.mirea.newrav1k.accountservice.exception.AccountNotFoundException;
import ru.mirea.newrav1k.accountservice.exception.AccountServiceException;
import ru.mirea.newrav1k.accountservice.exception.InsufficientBalanceException;
import ru.mirea.newrav1k.accountservice.mapper.AccountMapper;
import ru.mirea.newrav1k.accountservice.model.dto.AccountCreateRequest;
import ru.mirea.newrav1k.accountservice.model.dto.AccountResponse;
import ru.mirea.newrav1k.accountservice.model.dto.AccountUpdateRequest;
import ru.mirea.newrav1k.accountservice.model.entity.Account;
import ru.mirea.newrav1k.accountservice.repository.AccountRepository;

import java.math.BigDecimal;
import java.util.ConcurrentModificationException;
import java.util.UUID;

import static ru.mirea.newrav1k.accountservice.utils.MessageCode.RETRY_EXHAUSTED;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountService {

    private final AccountRepository accountRepository;

    private final AccountMapper accountMapper;

    private final ObjectMapper objectMapper;

    public Page<AccountResponse> findAll(Pageable pageable) {
        log.info("Finding all accounts");
        return this.accountRepository.findAll(pageable)
                .map(this.accountMapper::toAccountResponse);
    }

    public AccountResponse findById(UUID accountId) {
        log.info("Finding account with id {}", accountId);
        return this.accountRepository.findById(accountId)
                .map(this.accountMapper::toAccountResponse)
                .orElseThrow(AccountNotFoundException::new);
    }

    @Transactional
    public AccountResponse create(AccountCreateRequest request) {
        log.info("Creating new account");
        Account account = buildAccountFromRequest(request);
        this.accountRepository.save(account);
        return this.accountMapper.toAccountResponse(account);
    }

    @Transactional
    public AccountResponse update(UUID accountId, AccountUpdateRequest request) {
        log.info("Updating account with id {}", accountId);
        return this.accountRepository.findById(accountId)
                .map(account -> {
                    account.setName(request.name());
                    account.setCurrency(request.currency());
                    account.setType(request.type());
                    return account;
                })
                .map(this.accountMapper::toAccountResponse)
                .orElseThrow(AccountNotFoundException::new);
    }

    @Transactional
    public AccountResponse update(UUID accountId, JsonNode jsonNode) {
        log.info("Updating account with id {}", accountId);
        Account account = findAccountByIdOrThrow(accountId);
        try {
            this.objectMapper.readerForUpdating(account).readValue(jsonNode);

            return this.accountMapper.toAccountResponse(account);
        } catch (Exception exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error while updating account", exception);
        }
    }

    @Transactional
    public void deleteById(UUID accountId) {
        log.info("Deleting account with id {}", accountId);
        this.accountRepository.deleteById(accountId);
    }

    @Transactional
    @Retry(name = "accountService", fallbackMethod = "updateBalanceFallback")
    public void updateBalance(UUID accountId, BigDecimal amount) {
        log.info("Updating account balance with id {}", accountId);
        Account account = findAccountByIdOrThrow(accountId);
        if (amount.signum() < 0) {
            account.withdraw(amount.abs());
        } else {
            account.deposit(amount);
        }
        this.accountRepository.save(account);
    }

    @Deprecated(forRemoval = true)
    @Transactional
    public void depositMoney(UUID accountId, BigDecimal amount) {
        log.info("Making {} money for account with id {}", amount, accountId);
        Account account = findAccountByIdOrThrow(accountId);
        account.deposit(amount);
    }

    @Deprecated(forRemoval = true)
    @Transactional
    public void withdrawMoney(UUID accountId, BigDecimal amount) {
        log.info("Withdrawing {} money for account with id {}", amount, accountId);
        Account account = findAccountByIdOrThrow(accountId);
        account.withdraw(amount);
    }

    @Transactional
    public void deactivateAccount(UUID accountId) {
        log.info("Deactivating account with id {}", accountId);
        Account account = findAccountByIdOrThrow(accountId);
        account.deactivate();
    }

    private Account buildAccountFromRequest(AccountCreateRequest request) {
        Account account = new Account();

        account.setUserId(request.userId());
        account.setName(request.name());
        account.setCurrency(request.currency());
        account.setType(request.type());

        account.setBalance(BigDecimal.ZERO);
        account.setActive(true);

        return account;
    }

    private Account findAccountByIdOrThrow(UUID accountId) {
        return this.accountRepository.findById(accountId)
                .orElseThrow(AccountNotFoundException::new);
    }

    private BigDecimal findAccountBalance(UUID accountId) {
        return this.accountRepository.findById(accountId)
                .map(Account::getBalance)
                .orElse(BigDecimal.ZERO);
    }

    private void updateBalanceFallback(UUID accountId, BigDecimal amount, Exception exception) {
        log.warn("Failed to update account balance with id {}", accountId, exception);
        if (exception instanceof ObjectOptimisticLockingFailureException) {
            throw new ConcurrentModificationException();
        } else if (exception instanceof InsufficientBalanceException) {
            throw new InsufficientBalanceException(new Object[]{amount, findAccountBalance(accountId)});
        } else if (exception instanceof AccountBalanceException abe) {
            throw new AccountBalanceException(abe.getMessageCode(), new BigDecimal[]{amount});
        }
        throw new AccountServiceException(RETRY_EXHAUSTED, new String[]{exception.getMessage()});
    }

}