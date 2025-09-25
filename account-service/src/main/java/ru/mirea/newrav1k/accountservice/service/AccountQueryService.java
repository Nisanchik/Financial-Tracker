package ru.mirea.newrav1k.accountservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mirea.newrav1k.accountservice.exception.AccountAccessDeniedException;
import ru.mirea.newrav1k.accountservice.exception.AccountNotFoundException;
import ru.mirea.newrav1k.accountservice.mapper.AccountMapper;
import ru.mirea.newrav1k.accountservice.model.dto.AccountFilter;
import ru.mirea.newrav1k.accountservice.model.dto.AccountResponse;
import ru.mirea.newrav1k.accountservice.model.entity.Account;
import ru.mirea.newrav1k.accountservice.repository.AccountRepository;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountQueryService {

    private final AccountRepository accountRepository;

    private final AccountMapper accountMapper;

    @PreAuthorize("hasRole('ADMIN')")
    public Page<AccountResponse> findAll(AccountFilter filter, Pageable pageable) {
        log.debug("Finding all accounts: filter={}", filter);
        Specification<Account> specification = this.accountRepository.buildAccountSpecification(filter);
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
    public Page<AccountResponse> findAllAccountsByTrackerId(UUID trackerId, AccountFilter filter, Pageable pageable) {
        log.debug("Finding all tracker accounts: filter={}", filter);
        AccountFilter updatedFilter = new AccountFilter(trackerId, filter.name(), filter.currency(), filter.createdAtFrom(), filter.createdAtTo());
        Specification<Account> specification = this.accountRepository.buildAccountSpecification(updatedFilter);
        return this.accountRepository.findAll(specification, pageable)
                .map(this.accountMapper::toAccountResponse);
    }

    @PreAuthorize("isAuthenticated()")
    @Cacheable(value = "account-details", key = "#trackerId + '-' + #accountId")
    public AccountResponse findByTrackerIdAndAccountId(UUID trackerId, UUID accountId) {
        log.debug("Finding account: trackerId={}, accountId={}", trackerId, accountId);
        return this.accountRepository.findAccountByTrackerIdAndId(trackerId, accountId)
                .map(this.accountMapper::toAccountResponse)
                .orElseThrow(AccountAccessDeniedException::new);
    }

}