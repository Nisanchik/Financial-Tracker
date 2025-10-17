package ru.mirea.nisanchik.categoryservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mirea.nisanchik.categoryservice.exception.CategoryServiceException;
import ru.mirea.nisanchik.categoryservice.model.entity.OutboxEvent;
import ru.mirea.nisanchik.categoryservice.model.enums.OutboxStatus;
import ru.mirea.nisanchik.categoryservice.repository.OutboxRepository;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OutboxService {
    private final OutboxRepository outboxRepository;

    private final ObjectMapper objectMapper;

    @Transactional
    public void saveEvent(
            String aggregateType,
            UUID aggregateId,
            String topic,
            String eventType,
            Object event
    ){
        log.info("Saving event");
        try {
            if (this.outboxRepository.existsByAggregateIdAndEventType(aggregateId, eventType)) return;
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

        } catch (JsonProcessingException e) {
            throw new CategoryServiceException("Failed to serialize event", e, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
