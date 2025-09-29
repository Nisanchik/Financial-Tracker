package ru.mirea.newrav1k.transactionservice.event.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import ru.mirea.newrav1k.transactionservice.event.CompensateDifferenceAmountEvent;
import ru.mirea.newrav1k.transactionservice.event.TransactionCancelledEvent;
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

    @EventListener(classes = TransactionCreatedEvent.class)
    public void handleTransactionCreatedEvent(TransactionCreatedEvent event) {
        log.debug("Transaction created event: {}", event);
        this.balanceService.updateBalance(event.transactionId(), event.accountId(), event.type(), event.amount());
        this.transactionEventPublisher.publishExternalTransactionSuccessCreatedEvent(event.transactionId());
    }

    @TransactionalEventListener(classes = TransactionCancelledEvent.class)
    public void handleTransactionCancelledEvent(TransactionCancelledEvent event) {
        log.debug("Transaction cancelled event: {}", event);
        this.balanceService.compensateTransaction(
                event.compensationId(),
                event.accountId(),
                event.type(),
                event.amount()
        );
        this.transactionService.updateTransactionStatus(event.transactionId(), TransactionStatus.CANCELLED);
    }

    @TransactionalEventListener(classes = CompensateDifferenceAmountEvent.class)
    public void handleCompensateDifferenceAmountEvent(CompensateDifferenceAmountEvent event) {
        log.debug("Compensate difference amount event: {}", event);
        this.balanceService.compensateDifferenceAmount(
                event.compensationId(),
                event.accountId(),
                event.transactionType(),
                event.oldAmount(),
                event.newAmount()
        );
        this.transactionService.updateTransactionStatus(event.transactionId(), TransactionStatus.COMPLETED);
    }

}