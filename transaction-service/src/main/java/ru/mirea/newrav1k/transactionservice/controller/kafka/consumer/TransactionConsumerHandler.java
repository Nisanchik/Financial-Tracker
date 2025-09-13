package ru.mirea.newrav1k.transactionservice.controller.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import ru.mirea.newrav1k.transactionservice.event.BalanceUpdateFailureEvent;
import ru.mirea.newrav1k.transactionservice.event.TransactionCompensateDifferenceAmountEvent;
import ru.mirea.newrav1k.transactionservice.event.TransactionCompensateEvent;
import ru.mirea.newrav1k.transactionservice.event.TransactionSuccessCreatedEvent;
import ru.mirea.newrav1k.transactionservice.model.entity.ProcessedEvent;
import ru.mirea.newrav1k.transactionservice.model.enums.TransactionStatus;
import ru.mirea.newrav1k.transactionservice.service.BalanceService;
import ru.mirea.newrav1k.transactionservice.service.ProcessedEventService;
import ru.mirea.newrav1k.transactionservice.service.TransactionService;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionConsumerHandler {

    private final ProcessedEventService processedEventService;

    private final TransactionService transactionService;

    private final BalanceService balanceService;

    @KafkaListener(topics = "${transaction-service.kafka.topics.transaction-successfully-created}",
            groupId = "${transaction-service.kafka.group-id}", containerFactory = "kafkaListenerContainerFactory")
    public void handleTransactionSuccessCreated(@Payload TransactionSuccessCreatedEvent event) {
        log.debug("Handling TransactionSuccessCreatedEvent {}", event);
        try {
            this.transactionService.updateTransactionStatus(event.transactionId(), TransactionStatus.COMPLETED);

            this.processedEventService.markEventAsProcessed(new ProcessedEvent(event.eventId()));
        } catch (DataIntegrityViolationException exception) {
            log.info("TransactionSuccessCreatedEvent {} successfully processed, skipping", event.eventId());
        } catch (Exception exception) {
            log.error("TransactionSuccessCreatedEvent {} failed", event.eventId(), exception);
            throw exception;
        }
    }

    @KafkaListener(topics = "${transaction-service.kafka.topics.transaction-balance-failure}",
            groupId = "${transaction-service.kafka.group-id}", containerFactory = "kafkaListenerContainerFactory")
    public void handleBalanceUpdateFailure(@Payload BalanceUpdateFailureEvent event) {
        log.debug("Handling BalanceUpdateFailureEvent {}", event);
        try {
            this.balanceService.compensateTransaction(event.transactionId(), event.accountId(), event.type(), event.amount());

            this.transactionService.updateTransactionStatus(event.transactionId(), TransactionStatus.FAILED);

            this.processedEventService.markEventAsProcessed(new ProcessedEvent(event.eventId()));
        } catch (DataIntegrityViolationException exception) {
            log.info("TransactionBalanceFailureEvent {} successfully processed, skipping", event.eventId());
        } catch (Exception exception) {
            log.error("Error while handling BalanceUpdateFailureEvent {}", event.eventId(), exception);
            throw exception;
        }
    }

    @KafkaListener(topics = "${transaction-service.kafka.topics.transaction-compensate}",
            groupId = "${transaction-service.kafka.group-id}", containerFactory = "kafkaListenerContainerFactory")
    public void handleCompensateTransaction(@Payload TransactionCompensateEvent event) {
        log.debug("Handling CompensationEvent {}", event);
        try {
            this.balanceService.compensateTransaction(event.transactionId(), event.accountId(), event.type(), event.amount());

            this.processedEventService.markEventAsProcessed(new ProcessedEvent(event.eventId()));
        } catch (DataIntegrityViolationException exception) {
            log.info("TransactionCompensationEvent {} successfully processed, skipping", event.eventId());
        } catch (Exception exception) {
            log.error("Error while handling CompensationEvent {}", event.eventId(), exception);
            throw exception;
        }
    }

    @KafkaListener(topics = "${transaction-service.kafka.topics.transaction-compensate-difference-amount}",
            groupId = "${transaction-service.kafka.group-id}", containerFactory = "kafkaListenerContainerFactory")
    public void handleTransactionCompensateDifferenceAmount(@Payload TransactionCompensateDifferenceAmountEvent event) {
        log.debug("Handling TransactionCompensateDifferenceAmountEvent {}", event);
        try {
            this.balanceService.compensateDifferenceAmount(
                    event.compensationId(),
                    event.accountId(),
                    event.transactionType(),
                    event.oldAmount(),
                    event.newAmount()
            );

            this.processedEventService.markEventAsProcessed(new ProcessedEvent(event.compensationId()));
        } catch (DataIntegrityViolationException exception) {
            log.info("TransactionCompensateDifferenceAmountEvent {} successfully processed, skipping", event.compensationId());
        } catch (Exception exception) {
            log.error("Error while handling TransactionCompensateDifferenceAmountEvent {}", event.compensationId(), exception);
            throw exception;
        }
    }

}