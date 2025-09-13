package ru.mirea.newrav1k.transactionservice.controller.kafka.producer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.mirea.newrav1k.transactionservice.model.entity.OutboxEvent;

import java.util.concurrent.ExecutionException;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionProducerHandler {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void send(OutboxEvent event) throws ExecutionException, InterruptedException {
        this.kafkaTemplate.send(event.getTopic(), event.getAggregateId().toString(), event).get();
    }

}