package ru.mirea.newrav1k.accountservice.controller.rest;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import ru.mirea.newrav1k.accountservice.model.dto.AccountCreateRequest;
import ru.mirea.newrav1k.accountservice.model.dto.AccountResponse;
import ru.mirea.newrav1k.accountservice.model.dto.AccountUpdateRequest;
import ru.mirea.newrav1k.accountservice.service.AccountService;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    @GetMapping
    public PagedModel<AccountResponse> loadAllAccounts(@PageableDefault Pageable pageable) {
        log.info("Loading all accounts");
        return new PagedModel<>(this.accountService.findAll(pageable));
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<AccountResponse> loadAccount(@PathVariable("accountId") UUID accountId) {
        log.info("Loading account with id {}", accountId);
        AccountResponse account = this.accountService.findById(accountId);
        return ResponseEntity.ok(account);
    }

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@RequestBody AccountCreateRequest request,
                                                         UriComponentsBuilder uriBuilder) {
        log.info("Creating account {}", request);
        AccountResponse account = this.accountService.create(request);
        return ResponseEntity.created(uriBuilder
                        .replacePath("/api/account/{accountId}")
                        .build(account.id()))
                .body(account);
    }

    @PutMapping("/{accountId}")
    public ResponseEntity<AccountResponse> updateAccount(@PathVariable("accountId") UUID accountId,
                                                         @RequestBody AccountUpdateRequest request) {
        log.info("Updating account {}", request);
        AccountResponse account = this.accountService.update(accountId, request);
        return ResponseEntity.ok(account);
    }

    @PatchMapping("/{accountId}")
    public ResponseEntity<AccountResponse> patchAccount(@PathVariable("accountId") UUID accountId,
                                                        @RequestBody JsonNode jsonNode) {
        log.info("Updating account {}", accountId);
        AccountResponse account = this.accountService.update(accountId, jsonNode);
        return ResponseEntity.ok(account);
    }

    @DeleteMapping("/{accountId}")
    public ResponseEntity<Void> deleteAccount(@PathVariable("accountId") UUID accountId) {
        log.info("Deleting account {}", accountId);
        this.accountService.deleteById(accountId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{accountId}/update-balance")
    public ResponseEntity<AccountResponse> updateAccountBalance(@PathVariable("accountId") UUID accountId,
                                                                @RequestParam("amount") BigDecimal amount) {
        log.info("Updating account balance for account {}", accountId);
        this.accountService.updateBalance(accountId, amount);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{accountId}/withdraw-balance")
    public ResponseEntity<Void> withdrawAccountBalance(@PathVariable("accountId") UUID accountId,
                                                       @RequestParam("amount") BigDecimal amount) {
        log.info("Withdrawing account {}", accountId);
        this.accountService.withdrawMoney(accountId, amount);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{accountId}/deposit-balance")
    public ResponseEntity<Void> depositAccountBalance(@PathVariable("accountId") UUID accountId,
                                                      @RequestParam("amount") BigDecimal amount) {
        log.info("Deposit account balance for account {}", accountId);
        this.accountService.depositMoney(accountId, amount);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{accountId}/deactivate")
    public ResponseEntity<Void> deactivateAccount(@PathVariable("accountId") UUID accountId) {
        log.info("Deactivating account {}", accountId);
        this.accountService.deactivateAccount(accountId);
        return ResponseEntity.noContent().build();
    }

}