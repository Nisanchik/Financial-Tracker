package ru.mirea.nisanchik.categoryservice.model.entity;

import jakarta.persistence.*;
import lombok.*;
import ru.mirea.nisanchik.categoryservice.model.enums.OutboxStatus;

import java.util.UUID;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "t_outbox_events",
        schema = "categories_management",
        indexes = {
                @Index(name = "idx_outbox_event_created", columnList = "status, createdAt"),
        }
)
public class OutboxEvent extends BaseEntity {

    @Column(nullable = false)
    private String aggregateType;

    @Column(nullable = false)
    private UUID aggregateId;

    @Column(nullable = false)
    private String topic;

    @Column(nullable = false)
    private String eventType;

    @Lob
    @Column(nullable = false)
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OutboxStatus status;
}
