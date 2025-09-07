package ru.mirea.newrav1k.transactionservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.mirea.newrav1k.transactionservice.exception.TransactionNotFoundException;
import ru.mirea.newrav1k.transactionservice.exception.TransactionServiceException;
import ru.mirea.newrav1k.transactionservice.mapper.TransactionMapper;
import ru.mirea.newrav1k.transactionservice.model.dto.TransactionCreateRequest;
import ru.mirea.newrav1k.transactionservice.model.dto.TransactionFilter;
import ru.mirea.newrav1k.transactionservice.model.dto.TransactionResponse;
import ru.mirea.newrav1k.transactionservice.model.dto.TransactionUpdateRequest;
import ru.mirea.newrav1k.transactionservice.model.entity.Transaction;
import ru.mirea.newrav1k.transactionservice.model.enums.TransactionType;
import ru.mirea.newrav1k.transactionservice.repository.TransactionRepository;
import ru.mirea.newrav1k.transactionservice.service.client.AccountClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static ru.mirea.newrav1k.transactionservice.utils.MessageCode.TRANSACTION_UPDATE_FAILED;

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
                .orElseThrow(TransactionNotFoundException::new);
    }

    @Transactional
    public TransactionResponse create(TransactionCreateRequest request) {
        log.info("Creating new transaction");
        Transaction transaction = buildTransactionFromRequest(request);
        this.transactionRepository.save(transaction);
        processUpdateBalance(transaction);
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
                .orElseThrow(TransactionNotFoundException::new);
    }

    @Transactional
    public TransactionResponse updateById(UUID transactionId, JsonNode jsonNode) {
        log.info("Updating transaction with id {}", transactionId);
        Transaction transaction = this.transactionRepository.findById(transactionId)
                .orElseThrow(TransactionNotFoundException::new);
        try {
            this.objectMapper.readerForUpdating(transaction).readValue(jsonNode);

            return this.transactionMapper.toTransactionResponse(transaction);
        } catch (Exception exception) {
            log.error("Error while updating transaction with id {}", transactionId, exception);
            throw new TransactionServiceException(TRANSACTION_UPDATE_FAILED);
        }
    }

    @Transactional
    public void deleteById(UUID transactionId) {
        log.info("Deleting transaction by id {}", transactionId);
        this.transactionRepository.findById(transactionId)
                .ifPresent(transaction -> {
                    this.accountClient.updateBalance(transaction.getAccountId(), transaction.getAmount());
                    this.transactionRepository.delete(transaction);
                });
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

    @CircuitBreaker(name = "transactionService", fallbackMethod = "processUpdateBalanceFallback")
    public void processUpdateBalance(Transaction transaction) {
        if (transaction.getType().equals(TransactionType.INCOME)) {
            this.accountClient.updateBalance(transaction.getAccountId(), transaction.getAmount());
        } else if (transaction.getType().equals(TransactionType.EXPENSE)) {
            this.accountClient.updateBalance(transaction.getAccountId(), transaction.getAmount().negate());
        }
    }

    private void processUpdateBalanceFallback(Transaction transaction, CallNotPermittedException ex) {
        log.warn("Processing update balance fallback");
        // TODO: fallback для CircuitBreaker
    }

    private void processUpdateBalanceFallback(Transaction transaction, Exception ex) {
        log.warn("Processing update balance fallback");
        // TODO: fallback для CircuitBreaker
    }

    @Deprecated(forRemoval = true)
    private void processPaymentTransaction(Transaction transaction) {
        if (transaction.getType().equals(TransactionType.INCOME)) {
            this.accountClient.depositBalance(transaction.getAccountId(), transaction.getAmount());
        } else if (transaction.getType().equals(TransactionType.EXPENSE)) {
            this.accountClient.withdrawBalance(transaction.getAccountId(), transaction.getAmount());
        }
    }

}