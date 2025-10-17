package ru.mirea.nisanchik.categoryservice.controller.kafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.mirea.nisanchik.categoryservice.model.entity.OutboxEvent;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Component
@RequiredArgsConstructor
public class CategoryProducerHandler {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private final ObjectMapper objectMapper;

    public void send(OutboxEvent event) {
        log.debug("Sending event: event={}, topic={}", event, event.getTopic());
        try {
            Class<?> clazz = Class.forName("ru.mirea.nisanchik.categoryservice.event." + event.getEventType());
            Object object = this.objectMapper.readValue(event.getPayload(), clazz);
            this.kafkaTemplate.send(event.getTopic(), event.getAggregateId().toString(), object)
                    .get(10L, TimeUnit.SECONDS);
            log.info("Successfully sent event: event={}, topic={}", event, event.getTopic());
        }catch (ClassNotFoundException exception) {
            log.error("Class not found: class={}", event.getEventType(), exception);
            throw new IllegalArgumentException("Event class not found", exception);
        } catch (JsonProcessingException exception) {
            log.error("Could not convert event to JSON: class={}, payload={}", event.getEventType(), event.getPayload(), exception);
            throw new IllegalArgumentException("Event payload could not be converted to JSON", exception);
        } catch (TimeoutException exception) {
            log.error("Timed out: class={}, payload={}", event.getEventType(), event.getPayload(), exception);
            throw new IllegalArgumentException("Timed out", exception);
        } catch (InterruptedException exception) {
            log.error("Interrupted exception: event={}", event, exception);
            throw new IllegalArgumentException("Interrupted while sending", exception);
        } catch (Exception exception) {
            log.error("Unexpected exception: event={}", event, exception);
            throw new IllegalArgumentException("Exception while sending", exception);
        }
    }
}