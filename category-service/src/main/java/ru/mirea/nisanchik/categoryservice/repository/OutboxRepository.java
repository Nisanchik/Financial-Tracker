package ru.mirea.nisanchik.categoryservice.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.mirea.nisanchik.categoryservice.model.entity.OutboxEvent;
import ru.mirea.nisanchik.categoryservice.model.enums.OutboxStatus;

import java.util.List;
import java.util.UUID;

@Repository
public interface OutboxRepository extends JpaRepository<OutboxEvent, UUID> {

    @EntityGraph(attributePaths = {"payload"})
    List<OutboxEvent> findTop50ByStatusOrderByCreatedAtAsc(OutboxStatus status);

    boolean existsByAggregateIdAndEventType(UUID aggregateId, String eventType);

}
