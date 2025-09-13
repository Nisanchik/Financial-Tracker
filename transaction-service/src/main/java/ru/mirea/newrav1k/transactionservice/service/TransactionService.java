package ru.mirea.newrav1k.transactionservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.mirea.newrav1k.transactionservice.event.publisher.TransactionEventPublisher;
import ru.mirea.newrav1k.transactionservice.exception.TransactionNotFoundException;
import ru.mirea.newrav1k.transactionservice.exception.TransactionProcessingException;
import ru.mirea.newrav1k.transactionservice.exception.TransactionServiceException;
import ru.mirea.newrav1k.transactionservice.exception.TransactionStatusException;
import ru.mirea.newrav1k.transactionservice.mapper.TransactionMapper;
import ru.mirea.newrav1k.transactionservice.model.dto.TransactionCreateRequest;
import ru.mirea.newrav1k.transactionservice.model.dto.TransactionFilter;
import ru.mirea.newrav1k.transactionservice.model.dto.TransactionResponse;
import ru.mirea.newrav1k.transactionservice.model.dto.TransactionUpdateRequest;
import ru.mirea.newrav1k.transactionservice.model.entity.Transaction;
import ru.mirea.newrav1k.transactionservice.model.enums.TransactionStatus;
import ru.mirea.newrav1k.transactionservice.repository.TransactionRepository;

import java.math.BigDecimal;
import java.util.UUID;

import static ru.mirea.newrav1k.transactionservice.utils.MessageCode.TRANSACTION_UPDATE_FAILED;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TransactionService {

    private final TransactionRepository transactionRepository;

    private final TransactionMapper transactionMapper;

    private final TransactionEventPublisher transactionEventPublisher;

    private final BalanceService balanceService;

    public Page<TransactionResponse> findAll(TransactionFilter filter, Pageable pageable) {
        log.debug("Request to get all transactions");
        Specification<Transaction> specification = this.transactionRepository.buildTransactionSpecification(filter);
        return this.transactionRepository.findAll(specification, pageable)
                .map(this.transactionMapper::toTransactionResponse);
    }

    public TransactionResponse findById(UUID transactionId) {
        log.debug("Request to get transaction by id {}", transactionId);
        return this.transactionRepository.findById(transactionId)
                .map(this.transactionMapper::toTransactionResponse)
                .orElseThrow(TransactionNotFoundException::new);
    }

    @Transactional
    public TransactionResponse create(TransactionCreateRequest request) {
        log.debug("Request to create a new transaction");
        Transaction transaction = savePendingTransaction(request);

        this.transactionEventPublisher.publishInternalTransactionCreatedEvent(
                transaction.getId(),
                transaction.getAccountId(),
                transaction.getType(),
                transaction.getAmount()
        );

        return this.transactionMapper.toTransactionResponse(transaction);
    }

    @Transactional
    public TransactionResponse updateById(UUID transactionId, TransactionUpdateRequest request) {
        log.debug("Request to update transaction by id {} and request {}", transactionId, request);
        return this.transactionRepository.findById(transactionId)
                .map(transaction -> {
                    if (request.description() != null) {
                        transaction.setDescription(request.description());
                    }
                    if (request.amount() != null) {
                        ensureNotCompletedTransaction(transaction);
                        compensateAmount(transaction, request.amount());
                    }
                    return transaction;
                })
                .map(this.transactionMapper::toTransactionResponse)
                .orElseThrow(TransactionNotFoundException::new);
    }

    @Transactional
    public TransactionResponse updateById(UUID transactionId, JsonNode jsonNode) {
        log.debug("Request to update transaction by id {} and jsonNode {}", transactionId, jsonNode);
        Transaction transaction = this.transactionRepository.findById(transactionId)
                .orElseThrow(TransactionNotFoundException::new);
        try {
            if (jsonNode.has("description")) {
                transaction.setDescription(jsonNode.get("description").asText());
            }
            if (jsonNode.has("amount")) {
                ensureNotCompletedTransaction(transaction);
                compensateAmount(transaction, jsonNode.decimalValue());
            }
            return this.transactionMapper.toTransactionResponse(transaction);
        } catch (Exception exception) {
            log.error("Error while updating transaction with id {}", transactionId, exception);
            throw new TransactionServiceException(TRANSACTION_UPDATE_FAILED);
        }
    }

    @Transactional
    public void deleteById(UUID transactionId) {
        log.debug("Request to delete transaction by id {}", transactionId);
        Transaction transaction = this.transactionRepository.findById(transactionId)
                .orElseThrow(TransactionNotFoundException::new);
        if (transaction.getStatus() == TransactionStatus.COMPLETED) {
            try {
                this.transactionEventPublisher.publishInternalTransactionCancelledEvent(
                        transaction.getId(),
                        transaction.getAccountId(),
                        transaction.getType(),
                        transaction.getAmount()
                );
            } catch (Exception exception) {
                log.error("Error while deleting transaction by id {}", transactionId, exception);
                throw new TransactionProcessingException();
            }
        }
        this.transactionRepository.delete(transaction);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateTransactionStatus(UUID transactionId, TransactionStatus status) {
        log.debug("Request to update status of transaction by id {}", transactionId);
        Transaction transaction = this.transactionRepository.findById(transactionId)
                .orElseThrow(TransactionNotFoundException::new);
        transaction.setStatus(status);
        this.transactionRepository.save(transaction);
    }

    private Transaction savePendingTransaction(TransactionCreateRequest request) {
        Transaction transaction = buildTransactionFromRequest(request);
        transaction.setStatus(TransactionStatus.PENDING);
        return this.transactionRepository.save(transaction);
    }

    private void compensateAmount(Transaction transaction, BigDecimal newAmount) {
        BigDecimal oldAmount = transaction.getAmount();
        BigDecimal delta = newAmount.subtract(oldAmount);
        this.balanceService.updateBalance(transaction.getId(), transaction.getAccountId(), transaction.getType(), delta);
        transaction.setAmount(newAmount);
    }

    private void ensureNotCompletedTransaction(Transaction transaction) {
        log.debug("Request to ensure transaction is completed");
        if (transaction.getStatus() == TransactionStatus.COMPLETED) {
            log.warn("Transaction with id {} already completed", transaction.getId());
            throw new TransactionStatusException();
        }
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

}