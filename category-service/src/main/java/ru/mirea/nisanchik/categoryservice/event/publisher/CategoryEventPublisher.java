package ru.mirea.nisanchik.categoryservice.event.publisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import ru.mirea.nisanchik.categoryservice.configuration.properties.CategoryTopicsProperties;
import ru.mirea.nisanchik.categoryservice.event.CategoryDeletedEvent;
import ru.mirea.nisanchik.categoryservice.service.OutboxService;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CategoryEventPublisher {

    private static final String AGGREGATE_TYPE_CATEGORY = "Category";

    private final OutboxService outboxService;

    private final CategoryTopicsProperties topics;

    private final ApplicationEventPublisher eventPublisher;

    public void publishInternalCategoryDeletedEvent(UUID categoryId) {
        CategoryDeletedEvent event = new CategoryDeletedEvent(
                UUID.randomUUID(),
                categoryId
        );
        log.info("Publishing category deleted event: {}", event);
        this.eventPublisher.publishEvent(event);
    }

    public void publishExternalCategoryDeletedEvent(UUID categoryId) {
        log.info("Publishing category deleted event: {}", categoryId);
        CategoryDeletedEvent event = new CategoryDeletedEvent(
                UUID.randomUUID(),
                categoryId
        );

        this.outboxService.saveEvent(AGGREGATE_TYPE_CATEGORY, categoryId,
                this.topics.categoryDelete(), CategoryDeletedEvent.class.getSimpleName(), event);
    }

}
