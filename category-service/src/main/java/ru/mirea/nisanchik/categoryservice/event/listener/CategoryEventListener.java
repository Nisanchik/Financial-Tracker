package ru.mirea.nisanchik.categoryservice.event.listener;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import ru.mirea.nisanchik.categoryservice.event.CategoryDeletedEvent;
import ru.mirea.nisanchik.categoryservice.event.publisher.CategoryEventPublisher;

@Slf4j
@Component
@RequiredArgsConstructor
public class CategoryEventListener {

    private final CategoryEventPublisher categoryEventPublisher;

    @TransactionalEventListener(classes = CategoryDeletedEvent.class)
    public void handleCategoryDeleted(CategoryDeletedEvent event) {
        this.categoryEventPublisher.publishExternalCategoryDeletedEvent(event.categoryId());
    }


}
