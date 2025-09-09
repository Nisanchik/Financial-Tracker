package ru.mirea.newrav1k.transactionservice.event.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import ru.mirea.newrav1k.transactionservice.event.BalanceUpdateFailureEvent;
import ru.mirea.newrav1k.transactionservice.event.TransactionCreatedEvent;
import ru.mirea.newrav1k.transactionservice.event.TransactionSuccessCreatedEvent;
import ru.mirea.newrav1k.transactionservice.model.entity.Transaction;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    public void publishTransactionCreatedEvent(Transaction transaction) {
        log.debug("Publishing transaction created event");
        TransactionCreatedEvent createdEvent = new TransactionCreatedEvent(
                UUID.randomUUID(),
                transaction.getId(),
                transaction.getAccountId(),
                transaction.getType(),
                transaction.getAmount()
        );
        this.eventPublisher.publishEvent(createdEvent);
    }

    public void publishTransactionSuccessCreatedEvent(UUID transactionId) {
        log.debug("Publishing transaction success created event");
        TransactionSuccessCreatedEvent successCreatedEvent = new TransactionSuccessCreatedEvent(
                UUID.randomUUID(),
                transactionId
        );
        this.eventPublisher.publishEvent(successCreatedEvent);
    }

    public void publishBalanceUpdateFailureEvent(UUID transactionId, UUID accountId) {
        log.debug("Publishing balance update failure event");
        BalanceUpdateFailureEvent failureEvent = new BalanceUpdateFailureEvent(
                UUID.randomUUID(),
                transactionId,
                accountId
        );
        this.eventPublisher.publishEvent(failureEvent);
    }

}