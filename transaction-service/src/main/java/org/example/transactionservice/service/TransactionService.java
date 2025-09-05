package org.example.transactionservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.transactionservice.exception.TransactionNotFoundException;
import org.example.transactionservice.mapper.TransactionMapper;
import org.example.transactionservice.model.dto.TransactionCreateRequest;
import org.example.transactionservice.model.dto.TransactionFilter;
import org.example.transactionservice.model.dto.TransactionResponse;
import org.example.transactionservice.model.dto.TransactionUpdateRequest;
import org.example.transactionservice.model.entity.Transaction;
import org.example.transactionservice.model.enums.TransactionType;
import org.example.transactionservice.repository.TransactionRepository;
import org.example.transactionservice.service.client.AccountClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static org.example.transactionservice.utils.MessageCode.TRANSACTION_NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionService {

    private final TransactionRepository transactionRepository;

    private final TransactionMapper transactionMapper;

    private final ObjectMapper objectMapper;

    private final AccountClient accountClient;

    public Page<TransactionResponse> findAll(TransactionFilter filter, Pageable pageable) {
        log.info("Finding all transactions by pageable");
        Specification<Transaction> specification = this.transactionRepository.buildTransactionSpecification(filter);
        return this.transactionRepository.findAll(specification, pageable)
                .map(this.transactionMapper::toTransactionResponse);
    }

    public TransactionResponse findById(UUID transactionId) {
        log.info("Finding transaction with id {}", transactionId);
        return this.transactionRepository.findById(transactionId)
                .map(this.transactionMapper::toTransactionResponse)
                .orElseThrow(() -> new TransactionNotFoundException(TRANSACTION_NOT_FOUND));
    }

    @Transactional
    public TransactionResponse create(TransactionCreateRequest request) {
        log.info("Creating new transaction");
        Transaction transaction = buildTransactionFromRequest(request);
        this.transactionRepository.save(transaction);
        processPaymentTransaction(transaction);
        return this.transactionMapper.toTransactionResponse(transaction);
    }

    @Transactional
    public TransactionResponse updateById(UUID transactionId, TransactionUpdateRequest request) {
        log.info("Updating transaction with id {}", transactionId);
        return this.transactionRepository.findById(transactionId)
                .map(transaction -> {
                    transaction.setDescription(request.description());
                    transaction.setAmount(request.amount());
                    return transaction;
                })
                .map(this.transactionMapper::toTransactionResponse)
                .orElseThrow(() -> new TransactionNotFoundException(TRANSACTION_NOT_FOUND));
    }

    @Transactional
    public TransactionResponse updateById(UUID transactionId, JsonNode jsonNode) {
        log.info("Updating transaction with id {}", transactionId);
        Transaction transaction = this.transactionRepository.findById(transactionId)
                .orElseThrow(() -> new TransactionNotFoundException(TRANSACTION_NOT_FOUND));
        try {
            this.objectMapper.readerForUpdating(transaction).readValue(jsonNode);

            return this.transactionMapper.toTransactionResponse(transaction);
        } catch (Exception exception) {
            log.error("Error while updating transaction with id {}", transactionId, exception);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error while updating transaction with id " + transactionId);
        }
    }

    @Transactional
    public void deleteById(UUID transactionId) {
        log.info("Deleting transaction by id {}", transactionId);
        this.transactionRepository.deleteById(transactionId);
    }

    private Transaction buildTransactionFromRequest(TransactionCreateRequest request) {
        Transaction transaction = new Transaction();

        transaction.setUserId(request.userId());
        transaction.setAmount(request.amount());
        transaction.setAccountId(request.accountId());
        transaction.setDescription(request.description());
        transaction.setCategoryId(request.categoryId());
        transaction.setType(request.type());
        transaction.setTags(request.tags());

        return transaction;
    }

    private void processPaymentTransaction(Transaction transaction) {
        if (transaction.getType().equals(TransactionType.INCOME)) {
            this.accountClient.depositBalance(transaction.getAccountId(), transaction.getAmount());
        } else if (transaction.getType().equals(TransactionType.EXPENSE)) {
            this.accountClient.withdrawBalance(transaction.getAccountId(), transaction.getAmount());
        }
    }

}