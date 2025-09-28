package ru.mirea.newrav1k.transactionservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.mirea.newrav1k.transactionservice.event.publisher.TransactionEventPublisher;
import ru.mirea.newrav1k.transactionservice.exception.TransactionAccessDeniedException;
import ru.mirea.newrav1k.transactionservice.exception.TransactionNotFoundException;
import ru.mirea.newrav1k.transactionservice.exception.TransactionProcessingException;
import ru.mirea.newrav1k.transactionservice.exception.TransactionServiceException;
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

    // TODO: добавить кэширование

    @PreAuthorize("hasRole('ADMIN')")
    public Page<TransactionResponse> findAll(TransactionFilter filter, Pageable pageable) {
        log.debug("Finding all transactions: filter={}", filter);
        Specification<Transaction> specification = this.transactionRepository.buildTransactionSpecification(filter);
        return this.transactionRepository.findAll(specification, pageable)
                .map(this.transactionMapper::toTransactionResponse);
    }

    @PreAuthorize("isAuthenticated()")
    public Page<TransactionResponse> findAllByTrackerId(UUID trackerId, TransactionFilter filter, Pageable pageable) {
        log.debug("Finding all transactions: trackerId={}, filter={}", trackerId, filter);
        TransactionFilter updatedFilter = new TransactionFilter(trackerId, filter.type(), filter.createdAtFrom(), filter.createdAtTo());
        Specification<Transaction> specification = this.transactionRepository.buildTransactionSpecification(updatedFilter);
        return this.transactionRepository.findAll(specification, pageable)
                .map(this.transactionMapper::toTransactionResponse);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public TransactionResponse findById(UUID trackerId, UUID transactionId) {
        log.debug("Finding transaction: trackerId={}, transactionId={}", trackerId, transactionId);
        return this.transactionRepository.findTransactionByTrackerIdAndId(trackerId, transactionId)
                .map(this.transactionMapper::toTransactionResponse)
                .orElseThrow(TransactionAccessDeniedException::new);
    }

    // TODO: сделать retry при возникновении ошибок (опционально)
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public TransactionResponse create(UUID trackerId, TransactionCreateRequest request) {
        log.debug("Creating transaction: trackerId={}, request={}", trackerId, request);
        Transaction transaction = savePendingTransaction(trackerId, request);

        this.transactionEventPublisher.publishInternalTransactionCreatedEvent(
                transaction.getId(),
                transaction.getAccountId(),
                transaction.getType(),
                transaction.getAmount()
        );
        // TODO: рассмотреть возможность сделать всё в рамках одной транзакции

        // TODO: сделать вывод сообщений при возникновении исключений на стороне account-service
        return this.transactionMapper.toTransactionResponse(transaction);
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional
    public TransactionResponse updateById(UUID trackerId, UUID transactionId, TransactionUpdateRequest request) {
        log.debug("Updating transaction: trackerId={}, transactionId={}, request={}", trackerId, transactionId, request);
        return this.transactionRepository.findTransactionByTrackerIdAndId(trackerId, transactionId)
                .map(transaction -> {
                    if (request.description() != null) {
                        transaction.setDescription(request.description());
                    }
                    if (request.amount() != null) {
                        if (!transaction.getAmount().equals(request.amount())) {
                            this.transactionEventPublisher.publishInternalCompensateDifferenceAmountEvent(
                                    transaction.getId(),
                                    transaction.getAccountId(),
                                    transaction.getType(),
                                    transaction.getAmount(),
                                    request.amount()
                            );
                            transaction.setAmount(request.amount());
                        } else {
                            log.warn("Transaction amount equals request amount {}", request.amount());
                        }
                    }
                    return transaction;
                })
                .map(this.transactionMapper::toTransactionResponse)
                .orElseThrow(TransactionAccessDeniedException::new);
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional
    public TransactionResponse updateById(UUID trackerId, UUID transactionId, JsonNode jsonNode) {
        log.debug("Updating transaction: trackerId={}, transactionId={}, jsonNode={}", trackerId, transactionId, jsonNode);
        Transaction transaction = findTransactionByTrackerIdAndId(trackerId, transactionId);
        try {
            if (jsonNode.has("description")) {
                transaction.setDescription(jsonNode.get("description").asText());
            }
            if (jsonNode.has("amount")) {
                BigDecimal requestAmount = jsonNode.get("amount").decimalValue();
                if (!transaction.getAmount().equals(requestAmount)) {
                    this.transactionEventPublisher.publishInternalCompensateDifferenceAmountEvent(
                            transaction.getId(),
                            transaction.getAccountId(),
                            transaction.getType(),
                            transaction.getAmount(),
                            requestAmount
                    );
                    transaction.setAmount(requestAmount);
                } else {
                    log.warn("Transaction amount equals request amount {}", requestAmount);
                }
            }
            return this.transactionMapper.toTransactionResponse(transaction);
        } catch (Exception exception) {
            log.error("Error while updating transaction with id {}", transactionId, exception);
            throw new TransactionServiceException(TRANSACTION_UPDATE_FAILED, HttpStatus.BAD_REQUEST);
        }
    }

    @PreAuthorize("isAuthenticated() or hasRole('ADMIN')")
    @Transactional
    public void deleteById(UUID trackerId, UUID transactionId) {
        log.debug("Deleting transaction: trackerId={}, transactionId={}", trackerId, transactionId);
        Transaction transaction = findTransactionByTrackerIdAndId(trackerId, transactionId);
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
        log.debug("Updating transaction status: transactionId={}, status={}", transactionId, status);
        Transaction transaction = this.transactionRepository.findById(transactionId)
                .orElseThrow(TransactionNotFoundException::new);
        transaction.setStatus(status);
        this.transactionRepository.save(transaction);
    }

    private Transaction savePendingTransaction(UUID trackerId, TransactionCreateRequest request) {
        Transaction transaction = buildTransactionFromRequest(trackerId, request);
        transaction.setStatus(TransactionStatus.PENDING);
        return this.transactionRepository.save(transaction);
    }

    private Transaction findTransactionByTrackerIdAndId(UUID trackerId, UUID transactionId) {
        return this.transactionRepository.findTransactionByTrackerIdAndId(trackerId, transactionId)
                .orElseThrow(TransactionAccessDeniedException::new);
    }

    private Transaction buildTransactionFromRequest(UUID trackerId, TransactionCreateRequest request) {
        Transaction transaction = new Transaction();

        transaction.setTrackerId(trackerId);
        transaction.setAmount(request.amount());
        transaction.setAccountId(request.accountId());
        transaction.setDescription(request.description());
        transaction.setCategoryId(request.categoryId());
        transaction.setType(request.type());
        transaction.setTags(request.tags());

        return transaction;
    }

}