package ru.mirea.newrav1k.transactionservice.event.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.mirea.newrav1k.transactionservice.configuration.properties.TransactionTopicsProperties;
import ru.mirea.newrav1k.transactionservice.event.BalanceUpdateFailureEvent;
import ru.mirea.newrav1k.transactionservice.event.TransactionCancelledEvent;
import ru.mirea.newrav1k.transactionservice.event.TransactionCreatedEvent;
import ru.mirea.newrav1k.transactionservice.event.TransactionSuccessCreatedEvent;
import ru.mirea.newrav1k.transactionservice.model.entity.Transaction;
import ru.mirea.newrav1k.transactionservice.service.OutboxService;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionEventPublisher {

    private static final String AGGREGATE_TYPE = "Transaction";

    private final OutboxService outboxService;

    private final TransactionTopicsProperties topics;

    public void publishTransactionCreatedEvent(Transaction transaction) {
        log.debug("Publishing TransactionCreatedEvent");
        TransactionCreatedEvent event = new TransactionCreatedEvent(
                UUID.randomUUID(),
                transaction.getId(),
                transaction.getAccountId(),
                transaction.getType(),
                transaction.getAmount()
        );

        this.outboxService.saveEvent(AGGREGATE_TYPE, transaction.getId(),
                this.topics.transactionCreated(), event.getClass().getSimpleName(), event);
    }

    public void publishTransactionCancelledEvent(UUID transactionId, UUID accountId,
                                                 TransactionType transactionType, BigDecimal amount) {
        log.debug("Publishing TransactionCancelledEvent");
        TransactionCancelledEvent event = new TransactionCancelledEvent(
                UUID.randomUUID(),
                transactionId,
                accountId,
                transactionType,
                amount
        );

        this.outboxService.saveEvent(AGGREGATE_TYPE, transaction.getId(),
                this.topics.transactionCancelled(), event.getClass().getSimpleName(), event);
    }

    public void publishTransactionSuccessCreatedEvent(UUID transactionId) {
        log.debug("Publishing TransactionSuccessCreatedEvent");
        TransactionSuccessCreatedEvent event = new TransactionSuccessCreatedEvent(
                UUID.randomUUID(),
                transactionId
        );

        this.outboxService.saveEvent(AGGREGATE_TYPE, transactionId,
                this.topics.transactionSuccessfullyCreated(), event.getClass().getSimpleName(), event);
    }

    public void publishBalanceUpdateFailureEvent(UUID transactionId, UUID accountId) {
        log.debug("Publishing BalanceUpdateFailureEvent");
        BalanceUpdateFailureEvent event = new BalanceUpdateFailureEvent(
                UUID.randomUUID(),
                transactionId,
                accountId
        );

        this.outboxService.saveEvent(AGGREGATE_TYPE, transactionId,
                this.topics.transactionBalanceFailure(), event.getClass().getSimpleName(), event);
    }

}