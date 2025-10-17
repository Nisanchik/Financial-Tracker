package ru.mirea.nisanchik.categoryservice.event.listener;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import ru.mirea.nisanchik.categoryservice.event.CategoryDeletedEvent;
import ru.mirea.nisanchik.categoryservice.event.publisher.CategoryEventPublisher;

@Slf4j
@Component
@RequiredArgsConstructor
public class CategoryEventListener {

    private final CategoryEventPublisher categoryEventPublisher;

    @EventListener(classes = CategoryDeletedEvent.class)
    public void handleCategoryDeleted(CategoryDeletedEvent event) {
        log.info("Category deleted event: {}", event);
        this.categoryEventPublisher.publishExternalCategoryDeletedEvent(event.categoryId());
    }
}
