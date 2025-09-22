package ru.mirea.newrav1k.transactionservice.controller.rest;

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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import ru.mirea.newrav1k.transactionservice.model.dto.TransactionCreateRequest;
import ru.mirea.newrav1k.transactionservice.model.dto.TransactionFilter;
import ru.mirea.newrav1k.transactionservice.model.dto.TransactionResponse;
import ru.mirea.newrav1k.transactionservice.model.dto.TransactionUpdateRequest;
import ru.mirea.newrav1k.transactionservice.security.HeaderAuthenticationDetails;
import ru.mirea.newrav1k.transactionservice.service.TransactionService;

import java.util.UUID;

@Tag(name = "Transaction Controller",
        description = "Контроллер для управления транзакциями")
@Slf4j
@RestController
@RequestMapping("/api/transactions")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionalService;

    @Operation(summary = "Получение всех транзакций",
            description = "Загружает все транзакции с использованием фильтра для поиска")
    @GetMapping
    public PagedModel<TransactionResponse> getAllTransactions(@AuthenticationPrincipal HeaderAuthenticationDetails authenticationDetails,
                                                              @Valid @ModelAttribute TransactionFilter filter,
                                                              @PageableDefault Pageable pageable) {
        log.info("Getting all transactions: filter={}", filter);
        Page<TransactionResponse> transactions =
                this.transactionalService.findAllByTrackerId(authenticationDetails.getTrackerId(), filter, pageable);
        return new PagedModel<>(transactions);
    }

    @Operation(summary = "Получение конкретной транзакции",
            description = "Загружает транзакцию по её идентификатору")
    @ApiResponse(responseCode = "404",
            description = "Транзакция не найдена")
    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionResponse> getTransaction(@AuthenticationPrincipal HeaderAuthenticationDetails authenticationDetails,
                                                              @PathVariable("transactionId") UUID transactionId) {
        log.info("Getting transaction: transactionId={}", transactionId);
        TransactionResponse transaction = this.transactionalService.findById(authenticationDetails.getTrackerId(), transactionId);
        return ResponseEntity.ok(transaction);
    }

    @Operation(summary = "Создание транзакции",
            description = "Создаёт новую транзакцию и производит списание/начисление денег на счёт")
    @ApiResponse(responseCode = "400",
            description = "Некорректный запрос")
    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(@AuthenticationPrincipal HeaderAuthenticationDetails authenticationDetails,
                                                                 @Valid @RequestBody TransactionCreateRequest request,
                                                                 UriComponentsBuilder uriBuilder) {
        log.info("Creating transaction: request={}", request);
        TransactionResponse transaction = this.transactionalService.create(authenticationDetails.getTrackerId(), request);
        return ResponseEntity.created(uriBuilder
                        .replacePath("/api/transaction/{transactionId}")
                        .build(transaction.id()))
                .body(transaction);
    }

    @Operation(summary = "Обновление транзакции",
            description = "Обновляет транзакцию по её уникальному идентификатору")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400",
                    description = "Некорректный запрос"),
            @ApiResponse(responseCode = "404",
                    description = "Транзакция не найдена")})
    @PutMapping("/{transactionId}")
    public ResponseEntity<TransactionResponse> updateTransaction(@AuthenticationPrincipal HeaderAuthenticationDetails authenticationDetails,
                                                                 @PathVariable("transactionId") UUID transactionId,
                                                                 @Valid @RequestBody TransactionUpdateRequest request) {
        log.info("Updating transaction: transactionId={}, request={}", transactionId, request);
        TransactionResponse transaction = this.transactionalService.updateById(authenticationDetails.getTrackerId(), transactionId, request);
        return ResponseEntity.ok(transaction);
    }

    @Operation(summary = "Частичное обновление транзакции",
            description = "Частично обновляет транзакцию по её уникальному идентификатору")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "400",
                    description = "Некорректный запрос"),
            @ApiResponse(responseCode = "404",
                    description = "Транзакция не найдена")})
    @PatchMapping("/{transactionId}")
    public ResponseEntity<TransactionResponse> partialUpdateTransaction(@AuthenticationPrincipal HeaderAuthenticationDetails authenticationDetails,
                                                                        @PathVariable("transactionId") UUID transactionId,
                                                                        @RequestBody JsonNode jsonNode) {
        log.info("Updated transaction: transactionId={}, jsonNode={}", transactionId, jsonNode);
        TransactionResponse transaction =
                this.transactionalService.updateById(authenticationDetails.getTrackerId(), transactionId, jsonNode);
        return ResponseEntity.ok(transaction);
    }

    @Operation(summary = "Удаление транзакции",
            description = "Удаляет транзакцию по её уникальному идентификатору и производит её откат")
    @DeleteMapping("/{transactionId}")
    public ResponseEntity<Void> deleteTransaction(@AuthenticationPrincipal HeaderAuthenticationDetails authenticationDetails,
                                                  @PathVariable("transactionId") UUID transactionId) {
        log.info("Deleting transaction: transactionId={}", transactionId);
        this.transactionalService.deleteById(authenticationDetails.getTrackerId(), transactionId);
        return ResponseEntity.noContent().build();
    }

}