package ru.mirea.newrav1k.transactionservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.mirea.newrav1k.transactionservice.exception.TransactionServiceException;
import ru.mirea.newrav1k.transactionservice.model.entity.OutboxEvent;
import ru.mirea.newrav1k.transactionservice.model.enums.OutboxStatus;
import ru.mirea.newrav1k.transactionservice.repository.OutboxRepository;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OutboxService {

    private final OutboxRepository outboxRepository;

    private final ObjectMapper objectMapper;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveEvent(
            String aggregateType,
            UUID aggregateId,
            String topic,
            String eventType,
            Object event
    ) {
        try {
            String payload = this.objectMapper.writeValueAsString(event);

            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .aggregateType(aggregateType)
                    .aggregateId(aggregateId)
                    .topic(topic)
                    .eventType(eventType)
                    .payload(payload)
                    .status(OutboxStatus.NEW)
                    .build();

            this.outboxRepository.save(outboxEvent);

            log.debug("Saved OutboxEvent: topic={}, eventType={}, aggregateId={}", topic, eventType, aggregateId);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event {}", event, e);
            throw new TransactionServiceException("Failed to serialize event", e);
        }
    }

}