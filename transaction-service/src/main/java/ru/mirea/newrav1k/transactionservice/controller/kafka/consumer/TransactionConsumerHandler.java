package ru.mirea.newrav1k.transactionservice.controller.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import ru.mirea.newrav1k.transactionservice.event.BalanceUpdateFailureEvent;
import ru.mirea.newrav1k.transactionservice.event.TransactionCancelledEvent;
import ru.mirea.newrav1k.transactionservice.event.TransactionCreatedEvent;
import ru.mirea.newrav1k.transactionservice.event.TransactionSuccessCreatedEvent;
import ru.mirea.newrav1k.transactionservice.event.publisher.TransactionEventPublisher;
import ru.mirea.newrav1k.transactionservice.model.enums.TransactionStatus;
import ru.mirea.newrav1k.transactionservice.service.BalanceService;
import ru.mirea.newrav1k.transactionservice.service.TransactionService;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionConsumerHandler {

    private final BalanceService balanceService;

    private final TransactionService transactionService;

    private final TransactionEventPublisher transactionEventPublisher;

    @KafkaListener(topics = "${transaction-service.kafka.topics.transaction-created}",
            groupId = "${transaction-service.kafka.group-id}")
    public void handleTransactionCreated(@Payload TransactionCreatedEvent event) {
        log.debug("Handling TransactionCreatedEvent {}", event);
        try {
            this.balanceService.updateBalance(event.accountId(), event.type(), event.amount());

            this.transactionEventPublisher.publishTransactionSuccessCreatedEvent(event.transactionId());
        } catch (Exception exception) {
            log.error("Error while handling TransactionCreatedEvent", exception);
            this.transactionEventPublisher.publishBalanceUpdateFailureEvent(event.transactionId(), event.accountId());
        }
    }

    @KafkaListener(topics = "${transaction-service.kafka.topics.transaction-cancelled}",
            groupId = "${transaction-service.kafka.group-id}")
    public void handleTransactionCancelled(@Payload TransactionCancelledEvent event) {
        log.debug("Handling TransactionCancelledEvent {}", event);
        this.balanceService.compensateTransaction(event.accountId(), event.type(), event.amount());
    }

    @KafkaListener(topics = "${transaction-service.kafka.topics.transaction-successfully-created}",
            groupId = "${transaction-service.kafka.group-id}")
    public void handleTransactionSuccessCreated(@Payload TransactionSuccessCreatedEvent event) {
        log.debug("Handling TransactionSuccessCreatedEvent {}", event);
        this.transactionService.updateTransactionStatus(event.transactionId(), TransactionStatus.COMPLETED);
    }

    @KafkaListener(topics = "${transaction-service.kafka.topics.transaction-balance-failure}",
            groupId = "${transaction-service.kafka.group-id}")
    public void handleBalanceUpdateFailure(@Payload BalanceUpdateFailureEvent event) {
        log.debug("Handling BalanceUpdateFailureEvent {}", event);
        this.transactionService.updateTransactionStatus(event.transactionId(), TransactionStatus.FAILED);
    }

}