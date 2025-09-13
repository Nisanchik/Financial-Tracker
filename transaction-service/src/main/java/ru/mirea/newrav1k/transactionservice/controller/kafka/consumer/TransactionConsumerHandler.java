package ru.mirea.newrav1k.transactionservice.controller.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import ru.mirea.newrav1k.transactionservice.event.BalanceUpdateFailureEvent;
import ru.mirea.newrav1k.transactionservice.event.TransactionSuccessCreatedEvent;
import ru.mirea.newrav1k.transactionservice.model.entity.ProcessedEvent;
import ru.mirea.newrav1k.transactionservice.model.enums.TransactionStatus;
import ru.mirea.newrav1k.transactionservice.service.ProcessedEventService;
import ru.mirea.newrav1k.transactionservice.service.TransactionService;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionConsumerHandler {

    private final ProcessedEventService processedEventService;

    private final TransactionService transactionService;

    @KafkaListener(topics = "${transaction-service.kafka.topics.transaction-successfully-created}",
            groupId = "${transaction-service.kafka.group-id}", containerFactory = "kafkaListenerContainerFactory")
    public void handleTransactionSuccessCreated(@Payload TransactionSuccessCreatedEvent event) {
        log.debug("Handling TransactionSuccessCreatedEvent {}", event);
        try {
            this.transactionService.updateTransactionStatus(event.transactionId(), TransactionStatus.COMPLETED);
            this.processedEventService.markEventAsProcessed(new ProcessedEvent(event.eventId()));
        } catch (DataIntegrityViolationException exception) {
            log.info("TransactionSuccessCreatedEvent {} successfully processed, skipping", event.eventId());
        }
    }

    @KafkaListener(topics = "${transaction-service.kafka.topics.transaction-balance-failure}",
            groupId = "${transaction-service.kafka.group-id}", containerFactory = "kafkaListenerContainerFactory")
    public void handleBalanceUpdateFailure(@Payload BalanceUpdateFailureEvent event) {
        log.debug("Handling BalanceUpdateFailureEvent {}", event);
        try {
            this.transactionService.updateTransactionStatus(event.transactionId(), TransactionStatus.FAILED);
            this.processedEventService.markEventAsProcessed(new ProcessedEvent(event.eventId()));
        } catch (DataIntegrityViolationException exception) {
            log.info("TransactionBalanceFailureEvent {} successfully processed, skipping", event.eventId());
        }
    }

}