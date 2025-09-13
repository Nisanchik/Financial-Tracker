package ru.mirea.newrav1k.transactionservice.event.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import ru.mirea.newrav1k.transactionservice.configuration.properties.TransactionTopicsProperties;
import ru.mirea.newrav1k.transactionservice.event.BalanceUpdateFailureEvent;
import ru.mirea.newrav1k.transactionservice.event.TransactionCancelledEvent;
import ru.mirea.newrav1k.transactionservice.event.TransactionCreatedEvent;
import ru.mirea.newrav1k.transactionservice.event.TransactionSuccessCreatedEvent;
import ru.mirea.newrav1k.transactionservice.model.entity.Transaction;
import ru.mirea.newrav1k.transactionservice.model.enums.TransactionType;
import ru.mirea.newrav1k.transactionservice.service.OutboxService;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionEventPublisher {

    private static final String AGGREGATE_TYPE = "Transaction";

    private final OutboxService outboxService;

    private final TransactionTopicsProperties topics;

    private final ApplicationEventPublisher eventPublisher;

    public void publishInternalTransactionCreatedEvent(Transaction transaction) {
        log.debug("Publishing TransactionCreatedEvent");
        TransactionCreatedEvent event = new TransactionCreatedEvent(
                UUID.randomUUID(),
                transaction.getId(),
                transaction.getAccountId(),
                transaction.getType(),
                transaction.getAmount()
        );

        this.eventPublisher.publishEvent(event);
    }

    public void publishInternalTransactionCancelledEvent(UUID transactionId, UUID accountId,
                                                 TransactionType transactionType, BigDecimal amount) {
        log.debug("Publishing TransactionCancelledEvent");
        TransactionCancelledEvent event = new TransactionCancelledEvent(
                UUID.randomUUID(),
                transactionId,
                accountId,
                transactionType,
                amount
        );

        this.eventPublisher.publishEvent(event);
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