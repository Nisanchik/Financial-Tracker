package ru.mirea.newrav1k.accountservice.controller.rest;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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
import ru.mirea.newrav1k.accountservice.model.dto.AccountFilter;
import ru.mirea.newrav1k.accountservice.model.dto.AccountResponse;
import ru.mirea.newrav1k.accountservice.model.dto.AccountUpdateRequest;
import ru.mirea.newrav1k.accountservice.security.HeaderAuthenticationDetails;
import ru.mirea.newrav1k.accountservice.service.AccountService;

import java.math.BigDecimal;
import java.util.UUID;

@Tag(name = "Account Controller",
        description = "Контроллер для управления аккаунтами")
@Slf4j
@RestController
@RequestMapping("/api/accounts")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @Operation(summary = "Загрузка аккаунтов",
            description = "Загружает все аккаунты")
    @GetMapping
    public PagedModel<AccountResponse> getAllAccounts(@AuthenticationPrincipal HeaderAuthenticationDetails authentication,
                                                      @ModelAttribute AccountFilter filter,
                                                      @PageableDefault Pageable pageable) {
        log.info("Request to get all accounts");
        Page<AccountResponse> accounts = this.accountService.findAllAccountsByTrackerId(authentication.getTrackerId(), filter, pageable);
        return new PagedModel<>(accounts);
    }

    @Operation(summary = "Загрузка аккаунта",
            description = "Загружает аккаунт по его идентификатору")
    @ApiResponse(responseCode = "404",
            description = "Аккаунт не найден")
    @GetMapping("/{accountId}")
    public ResponseEntity<AccountResponse> getAccount(@AuthenticationPrincipal HeaderAuthenticationDetails authentication,
                                                      @PathVariable("accountId") UUID accountId) {
        log.info("Request to get account: accountId={}", accountId);
        AccountResponse account = this.accountService.findByTrackerIdAndAccountId(authentication.getTrackerId(), accountId);
        return ResponseEntity.ok(account);
    }

    @Operation(summary = "Создание аккаунта",
            description = "Создаёт новый аккаунт")
    @ApiResponse(responseCode = "400",
            description = "Некорректный запрос")
    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@AuthenticationPrincipal HeaderAuthenticationDetails authentication,
                                                         @Valid @RequestBody AccountCreateRequest request,
                                                         UriComponentsBuilder uriBuilder) {
        log.info("Request to create account: request={}", request);
        AccountResponse account = this.accountService.create(request, authentication.getTrackerId());
        return ResponseEntity.created(uriBuilder
                        .replacePath("/api/account/{accountId}")
                        .build(account.id()))
                .body(account);
    }

    @Operation(summary = "Обновление аккаунта",
            description = "Обновляет аккаунт по его идентификатору")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400",
                    description = "Некорректный запрос"),
            @ApiResponse(responseCode = "404",
                    description = "Аккаунт не найден")})
    @PutMapping("/{accountId}")
    public ResponseEntity<AccountResponse> updateAccount(@AuthenticationPrincipal HeaderAuthenticationDetails authentication,
                                                         @PathVariable("accountId") UUID accountId,
                                                         @Valid @RequestBody AccountUpdateRequest request) {
        log.info("Request to update account: accountId={}, request={}", accountId, request);
        AccountResponse account = this.accountService.update(authentication.getTrackerId(), accountId, request);
        return ResponseEntity.ok(account);
    }

    @Operation(summary = "Частичное обновление аккаунта",
            description = "Частично обновляет аккаунт по его идентификатору")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400",
                    description = "Некорректный запрос"),
            @ApiResponse(responseCode = "404",
                    description = "Аккаунт не найден")})
    @PatchMapping("/{accountId}")
    public ResponseEntity<AccountResponse> patchAccount(@AuthenticationPrincipal HeaderAuthenticationDetails authentication,
                                                        @PathVariable("accountId") UUID accountId,
                                                        @RequestBody JsonNode jsonNode) {
        log.info("Request to patch account: accountId={}, jsonNode={}", accountId, jsonNode);
        AccountResponse account = this.accountService.update(authentication.getTrackerId(), accountId, jsonNode);
        return ResponseEntity.ok(account);
    }

    @Operation(summary = "Удаление аккаунта",
            description = "Удаляет аккаунт по его идентификатору")
    @DeleteMapping("/{accountId}")
    public ResponseEntity<Void> deleteAccount(@AuthenticationPrincipal HeaderAuthenticationDetails authentication,
                                              @PathVariable("accountId") UUID accountId) {
        log.info("Request to delete account: accountId={}", accountId);
        this.accountService.softDeleteById(authentication.getTrackerId(), accountId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Обновление баланса",
            description = """
                    Обновляет баланс аккаунта по его идентификатору.
                    В зависимости от знака производится пополнение/снятие денег с баланса""")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400",
                    description = "На счету не достаточно средств"),
            @ApiResponse(responseCode = "404",
                    description = "Аккаунт не найден")})
    @PostMapping("/{accountId}/update-balance") // @PostMapping для корректной работы FeignClient
    public ResponseEntity<Void> updateAccountBalance(@PathVariable("accountId") UUID accountId,
                                                     @RequestParam("transactionId") UUID transactionId,
                                                     @RequestParam("amount") BigDecimal amount,
                                                     @AuthenticationPrincipal HeaderAuthenticationDetails authentication) {
        log.info("Request to update account balance: accountId={}, transactionId={}", accountId, transactionId);
        this.accountService.updateBalance(authentication.getTrackerId(), accountId, transactionId, amount);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Снятие средств с баланса аккаунта",
            description = "Пополняет баланс аккаунта по его идентификатору", deprecated = true)
    @PostMapping("/{accountId}/withdraw-balance") // @PostMapping для корректной работы FeignClient
    public ResponseEntity<Void> withdrawAccountBalance(@PathVariable("accountId") UUID accountId,
                                                       @RequestParam("amount") BigDecimal amount,
                                                       @AuthenticationPrincipal HeaderAuthenticationDetails authentication) {
        log.info("Request to withdraw account balance: accountId={}", accountId);
        this.accountService.withdrawMoney(authentication.getTrackerId(), accountId, amount);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Пополнение баланса аккаунта",
            description = "Пополняет баланс аккаунта по его идентификатору", deprecated = true)
    @PostMapping("/{accountId}/deposit-balance") // @PostMapping для корректной работы FeignClient
    public ResponseEntity<Void> depositAccountBalance(@PathVariable("accountId") UUID accountId,
                                                      @RequestParam("amount") BigDecimal amount,
                                                      @AuthenticationPrincipal HeaderAuthenticationDetails authentication) {
        log.info("Request to deposit account balance: accountId={}", accountId);
        this.accountService.depositMoney(authentication.getTrackerId(), accountId, amount);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Деактивация аккаунта",
            description = "Деактивирует аккаунт по его идентификатору")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400",
                    description = "На счету не должно быть средств"),
            @ApiResponse(responseCode = "404",
                    description = "Аккаунт не найден")})
    @PatchMapping("/{accountId}/deactivate")
    public ResponseEntity<Void> deactivateAccount(@PathVariable("accountId") UUID accountId,
                                                  @AuthenticationPrincipal HeaderAuthenticationDetails authentication) {
        log.info("Request to deactivate account: accountId={}", accountId);
        this.accountService.deactivateAccount(authentication.getTrackerId(), accountId);
        return ResponseEntity.noContent().build();
    }

}