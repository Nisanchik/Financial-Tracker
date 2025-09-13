package ru.mirea.newrav1k.transactionservice.event.listener;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import ru.mirea.newrav1k.transactionservice.event.TransactionCancelledEvent;
import ru.mirea.newrav1k.transactionservice.event.TransactionCompensateDifferenceAmountEvent;
import ru.mirea.newrav1k.transactionservice.event.TransactionCreatedEvent;
import ru.mirea.newrav1k.transactionservice.event.publisher.TransactionEventPublisher;
import ru.mirea.newrav1k.transactionservice.model.enums.TransactionStatus;
import ru.mirea.newrav1k.transactionservice.service.BalanceService;
import ru.mirea.newrav1k.transactionservice.service.TransactionService;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionEventListener {

    private final BalanceService balanceService;

    private final TransactionService transactionService;

    private final TransactionEventPublisher transactionEventPublisher;

    @CircuitBreaker(name = "transactionCreatedEventListener", fallbackMethod = "handleTransactionCreatedEventFallback")
    @TransactionalEventListener(classes = TransactionCreatedEvent.class)
    public void handleTransactionCreatedEvent(TransactionCreatedEvent event) {
        log.debug("Transaction created event: {}", event);
        this.balanceService.updateBalance(event.transactionId(), event.accountId(), event.type(), event.amount());
        this.transactionEventPublisher.publishTransactionSuccessCreatedEvent(event.transactionId());
    }

    private void handleTransactionCreatedEventFallback(TransactionCreatedEvent event, Throwable throwable) {
        log.error("Transaction created event failed: {}", event, throwable);
        this.transactionEventPublisher.publishBalanceUpdateFailureEvent(
                event.transactionId(),
                event.accountId(),
                event.type(),
                event.amount()
        );
        // TODO: в случае реализовать отправку в отдельный топик для ручного регулирования
    }

    @CircuitBreaker(name = "transactionCancelledEventListener", fallbackMethod = "handleTransactionCancelledEventFallback")
    @TransactionalEventListener(classes = TransactionCancelledEvent.class)
    public void handleTransactionCancelledEvent(TransactionCancelledEvent event) {
        log.debug("Transaction cancelled event: {}", event);
        this.balanceService.compensateTransaction(event.compensationId(), event.accountId(), event.type(), event.amount());
        this.transactionService.updateTransactionStatus(event.transactionId(), TransactionStatus.CANCELLED);
    }

    private void handleTransactionCancelledEventFallback(TransactionCancelledEvent event, Throwable throwable) {
        log.error("Transaction cancelled event: {}", event, throwable);
        this.transactionService.updateTransactionStatus(event.transactionId(), TransactionStatus.FAILED);
        // TODO: в случае реализовать отправку в отдельный топик для ручного регулирования
    }

    @CircuitBreaker(name = "transactionCompensateDifferenceAmount", fallbackMethod = "handleTransactionCompensateDifferenceAmountFallback")
    @TransactionalEventListener(classes = TransactionCompensateDifferenceAmountEvent.class)
    public void handleTransactionCompensateDifferenceAmountEvent(TransactionCompensateDifferenceAmountEvent event) {
        log.debug("Transaction compensate difference amount event: {}", event);
        this.transactionEventPublisher.publishTransactionCompensateDifferenceAmountEvent(
                event.transactionId(),
                event.accountId(),
                event.transactionType(),
                event.oldAmount(),
                event.newAmount()
        );
    }

    private void handleTransactionCompensateDifferenceAmountFallback(TransactionCompensateDifferenceAmountEvent event, Throwable throwable) {
        log.error("Transaction compensate difference amount fallback event: {}", event, throwable);
        this.transactionEventPublisher.publishTransactionCompensateDifferenceAmountEvent(
                event.transactionId(),
                event.accountId(),
                event.transactionType(),
                event.oldAmount(),
                event.newAmount()
        );
        // TODO: в случае реализовать отправку в отдельный топик для ручного регулирования
    }

}