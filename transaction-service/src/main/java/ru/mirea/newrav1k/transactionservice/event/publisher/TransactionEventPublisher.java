package ru.mirea.newrav1k.transactionservice.event.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.mirea.newrav1k.transactionservice.configuration.properties.TransactionTopicsProperties;
import ru.mirea.newrav1k.transactionservice.event.BalanceUpdateFailureEvent;
import ru.mirea.newrav1k.transactionservice.event.CompensateDifferenceAmountEvent;
import ru.mirea.newrav1k.transactionservice.event.CompensateFailureEvent;
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
        log.debug("Publishing internal TransactionCreatedEvent");
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
        log.debug("Publishing internal TransactionCancelledEvent");
        TransactionCancelledEvent event = new TransactionCancelledEvent(
                UUID.randomUUID(),
                transactionId,
                accountId,
                transactionType,
                amount
        );

        this.eventPublisher.publishEvent(event);
    }

    public void publishInternalCompensateDifferenceAmountEvent(UUID transactionId, UUID accountId,
                                                               TransactionType transactionType, BigDecimal oldAmount, BigDecimal newAmount) {
        log.debug("Publishing internal CompensateDifferenceAmountEvent");
        CompensateDifferenceAmountEvent event = new CompensateDifferenceAmountEvent(
                UUID.randomUUID(),
                transactionId,
                accountId,
                transactionType,
                oldAmount,
                newAmount
        );

        this.eventPublisher.publishEvent(event);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void publishExternalTransactionSuccessCreatedEvent(UUID transactionId) {
        log.debug("Publishing TransactionSuccessCreatedEvent");
        TransactionSuccessCreatedEvent event = new TransactionSuccessCreatedEvent(
                UUID.randomUUID(),
                transactionId
        );

        this.outboxService.saveEvent(AGGREGATE_TYPE_TRANSACTION, transactionId,
                this.topics.transactionSuccessfullyCreated(), TransactionSuccessCreatedEvent.class.getSimpleName(), event);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void publishExternalBalanceUpdateFailureEvent(UUID transactionId, UUID accountId,
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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void publishExternalTransactionCompensateEvent(UUID transactionId, UUID accountId,
                                                          TransactionType transactionType, BigDecimal amount) {
        log.debug("Publishing CompensationEvent");
        TransactionCompensateEvent event = new TransactionCompensateEvent(
                UUID.randomUUID(),
                transactionId,
                accountId,
                transactionType,
                amount
        );

        this.outboxService.saveEvent(AGGREGATE_TYPE_TRANSACTION, transactionId,
                this.topics.transactionCompensate(), TransactionCompensateEvent.class.getSimpleName(), event);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void publishExternalCompensateFailureEvent(UUID transactionId, UUID accountId,
                                                      TransactionType transactionType, BigDecimal amount) {
        log.debug("Publishing CompensationFailureEvent");
        CompensateFailureEvent event = new CompensateFailureEvent(
                UUID.randomUUID(),
                transactionId,
                accountId,
                transactionType,
                amount
        );

        this.outboxService.saveEvent(AGGREGATE_TYPE_TRANSACTION, transactionId,
                this.topics.transactionCompensateFailure(), CompensateFailureEvent.class.getSimpleName(), event);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void publishExternalCompensateDifferenceAmountEvent(UUID transactionId, UUID accountId,
                                                               TransactionType transactionType, BigDecimal oldAmount, BigDecimal newAmount) {
        log.debug("Publishing CompensateDifferenceAmountEvent");
        CompensateDifferenceAmountEvent event = new CompensateDifferenceAmountEvent(
                UUID.randomUUID(),
                transactionId,
                accountId,
                transactionType,
                oldAmount,
                newAmount
        );

        this.outboxService.saveEvent(AGGREGATE_TYPE_TRANSACTION, transactionId,
                this.topics.transactionCompensateDifferenceAmount(), CompensateDifferenceAmountEvent.class.getSimpleName(), event);
    }

}