package org.example.transactionservice.controller.rest;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.transactionservice.model.dto.TransactionCreateRequest;
import org.example.transactionservice.model.dto.TransactionFilter;
import org.example.transactionservice.model.dto.TransactionResponse;
import org.example.transactionservice.model.dto.TransactionUpdateRequest;
import org.example.transactionservice.service.TransactionService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
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

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionalService;

    @GetMapping
    public PagedModel<TransactionResponse> loadAllTransactions(@ModelAttribute TransactionFilter filter,
                                                               @PageableDefault Pageable pageable) {
        log.info("Load all transactions");
        return new PagedModel<>(this.transactionalService.findAll(filter, pageable));
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionResponse> loadTransaction(@PathVariable("transactionId") UUID transactionId) {
        log.info("Load transaction with id: {}", transactionId);
        TransactionResponse transaction = this.transactionalService.findById(transactionId);
        return ResponseEntity.ok(transaction);
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> createTransaction(@RequestBody TransactionCreateRequest request,
                                                                 UriComponentsBuilder uriBuilder) {
        log.info("Create new transaction");
        TransactionResponse transaction = this.transactionalService.create(request);
        return ResponseEntity.created(uriBuilder
                        .replacePath("/api/transaction/{transactionId}")
                        .build(transaction.id()))
                .body(transaction);
    }

    @PutMapping("/{transactionId}")
    public ResponseEntity<TransactionResponse> updateTransaction(@PathVariable("transactionId") UUID transactionId,
                                                                 TransactionUpdateRequest request) {
        log.info("Update transaction with id: {}", transactionId);
        TransactionResponse transaction = this.transactionalService.updateById(transactionId, request);
        return ResponseEntity.ok(transaction);
    }

    @PatchMapping("/{transactionId}")
    public ResponseEntity<TransactionResponse> partialUpdateTransaction(@PathVariable("transactionId") UUID transactionId,
                                                                        @RequestBody JsonNode jsonNode) {
        log.info("Partial update transaction with id: {}", transactionId);
        TransactionResponse transaction = this.transactionalService.updateById(transactionId, jsonNode);
        return ResponseEntity.ok(transaction);
    }

    @DeleteMapping("/{transactionId}")
    public ResponseEntity<Void> deleteTransaction(@PathVariable("transactionId") UUID transactionId) {
        log.info("Delete transaction with id: {}", transactionId);
        this.transactionalService.deleteById(transactionId);
        return ResponseEntity.noContent().build();
    }

}