package ru.mirea.newrav1k.transactionservice.event.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import ru.mirea.newrav1k.transactionservice.event.BalanceUpdateFailureEvent;
import ru.mirea.newrav1k.transactionservice.event.TransactionCreatedEvent;
import ru.mirea.newrav1k.transactionservice.event.TransactionSuccessCreatedEvent;
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

    @Async
    @TransactionalEventListener(classes = TransactionCreatedEvent.class, phase = TransactionPhase.AFTER_COMMIT)
    public void handleTransactionCreated(final TransactionCreatedEvent event) {
        log.debug("Handling TransactionCreatedEvent {}", event);
        try {
            this.balanceService.updateBalance(event.accountId(), event.type(), event.amount());
            this.transactionEventPublisher.publishTransactionSuccessCreatedEvent(event.transactionId());
        } catch (Exception exception) {
            log.warn("Error while handling TransactionCreatedEvent", exception);
            this.transactionService.updateTransactionStatus(event.transactionId(), TransactionStatus.FAILED);
            this.transactionEventPublisher.publishBalanceUpdateFailureEvent(event.transactionId(), event.accountId());
        }
    }

    @EventListener(classes = BalanceUpdateFailureEvent.class)
    public void handleBalanceUpdateFailure(final BalanceUpdateFailureEvent event) {
        log.debug("Handling BalanceUpdateFailureEvent {}", event);
        this.transactionService.updateTransactionStatus(event.transactionId(), TransactionStatus.FAILED);
    }

    @EventListener(classes = TransactionSuccessCreatedEvent.class)
    public void handleTransactionSuccessCreated(final TransactionSuccessCreatedEvent event) {
        log.debug("Handling TransactionSuccessCreatedEvent {}", event);
        this.transactionService.updateTransactionStatus(event.transactionId(), TransactionStatus.COMPLETED);
    }

}