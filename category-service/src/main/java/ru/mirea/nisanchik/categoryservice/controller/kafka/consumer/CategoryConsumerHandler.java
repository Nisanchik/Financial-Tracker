package ru.mirea.nisanchik.categoryservice.controller.kafka.consumer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import ru.mirea.nisanchik.categoryservice.event.CategoryDeletedEvent;
import ru.mirea.nisanchik.categoryservice.model.entity.ProcessedEvent;
import ru.mirea.nisanchik.categoryservice.service.CategoryService;
import ru.mirea.nisanchik.categoryservice.service.ProcessedEventService;

@Slf4j
@Component
@RequiredArgsConstructor
public class CategoryConsumerHandler {
    private final ProcessedEventService processedEventService;

    private final CategoryService categoryService;

    @KafkaListener(topics = "${category-service.kafka.topics.category-delete}", groupId = "${category-service.kafka.group-id}", containerFactory = "kafkaListenerContainerFactory")
    public void handleCategorySuccessDeleted (@Payload final CategoryDeletedEvent categoryDeletedEvent) {
        //TODO: Даня допишешь
    }
}
