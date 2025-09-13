package ru.mirea.newrav1k.transactionservice.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import ru.mirea.newrav1k.transactionservice.controller.kafka.producer.TransactionProducerHandler;
import ru.mirea.newrav1k.transactionservice.model.entity.OutboxEvent;
import ru.mirea.newrav1k.transactionservice.model.enums.OutboxStatus;
import ru.mirea.newrav1k.transactionservice.repository.OutboxRepository;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxScheduler {

    private final OutboxRepository outboxRepository;

    private final TransactionProducerHandler producerHandler;

    private final TransactionTemplate transactionTemplate;

    @Transactional
    @Scheduled(fixedDelay = 5000)
    public void processOutboxEvents() {
        log.debug("Processing outbox events");
        List<OutboxEvent> events = this.outboxRepository.findTop50ByStatusOrderByCreatedAtAsc(OutboxStatus.NEW);
        for (OutboxEvent event : events) {
            this.transactionTemplate.executeWithoutResult(status -> {
                try {
                    event.setStatus(OutboxStatus.IN_PROGRESS);
                    this.outboxRepository.save(event);

                    this.producerHandler.send(event);
                    event.setStatus(OutboxStatus.PUBLISHED);

                    this.outboxRepository.save(event);
                } catch (Exception exception) {
                    event.setStatus(OutboxStatus.FAILED);
                    this.outboxRepository.save(event);
                }
            });
        }
    }

}