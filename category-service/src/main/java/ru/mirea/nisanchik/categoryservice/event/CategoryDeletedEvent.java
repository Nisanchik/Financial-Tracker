package ru.mirea.nisanchik.categoryservice.event;

import java.util.UUID;


public record CategoryDeletedEvent(
        UUID eventId,
        UUID categoryId
) {

}
