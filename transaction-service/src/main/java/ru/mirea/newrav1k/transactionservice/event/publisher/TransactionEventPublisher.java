package ru.mirea.newrav1k.transactionservice.event.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import ru.mirea.newrav1k.transactionservice.configuration.properties.TransactionTopicsProperties;
import ru.mirea.newrav1k.transactionservice.event.BalanceUpdateFailureEvent;
import ru.mirea.newrav1k.transactionservice.event.TransactionCancelledEvent;
import ru.mirea.newrav1k.transactionservice.event.TransactionCompensateEvent;
import ru.mirea.newrav1k.transactionservice.event.TransactionCreatedEvent;
import ru.mirea.newrav1k.transactionservice.event.TransactionSuccessCreatedEvent;
import ru.mirea.newrav1k.transactionservice.model.enums.TransactionType;
import ru.mirea.newrav1k.transactionservice.service.OutboxService;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionEventPublisher {

    private static final String AGGREGATE_TYPE_TRANSACTION = "Transaction";

    private final OutboxService outboxService;

    private final TransactionTopicsProperties topics;

    private final ApplicationEventPublisher eventPublisher;

    public void publishInternalTransactionCreatedEvent(UUID transactionId, UUID accountId,
                                                       TransactionType transactionType, BigDecimal amount) {
        log.debug("Publishing TransactionCreatedEvent");
        TransactionCreatedEvent event = new TransactionCreatedEvent(
                UUID.randomUUID(),
                transactionId,
                accountId,
                transactionType,
                amount
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

        this.outboxService.saveEvent(AGGREGATE_TYPE_TRANSACTION, transactionId,
                this.topics.transactionSuccessfullyCreated(), TransactionSuccessCreatedEvent.class.getSimpleName(), event);
    }

    public void publishBalanceUpdateFailureEvent(UUID transactionId, UUID accountId,
                                                 TransactionType transactionType, BigDecimal amount) {
        log.debug("Publishing BalanceUpdateFailureEvent");
        BalanceUpdateFailureEvent event = new BalanceUpdateFailureEvent(
                UUID.randomUUID(),
                transactionId,
                accountId,
                transactionType,
                amount
        );

        this.outboxService.saveEvent(AGGREGATE_TYPE_TRANSACTION, transactionId,
                this.topics.transactionBalanceFailure(), BalanceUpdateFailureEvent.class.getSimpleName(), event);
    }

    public void publishTransactionCompensateEvent(UUID transactionId, UUID accountId,
                                                  TransactionType transactionType, BigDecimal amount) {
        log.debug("Publishing CompensationEvent");
        TransactionCompensateEvent event = new TransactionCompensateEvent(
                UUID.randomUUID(),
                transactionId,
                accountId,
                transactionType,
                amount
        );

        this.outboxService.saveEvent(AGGREGATE_TYPE_TRANSACTION, accountId,
                this.topics.transactionCompensate(), TransactionCompensateEvent.class.getSimpleName(), event);
    }

}