package ru.mirea.nisanchik.categoryservice.model.entity;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "t_processed_events",
        schema = "category_management",
        indexes = {@Index(name = "idx_processed_event_ids", columnList = "eventId")}
)
public class ProcessedEvent {

    @Id
    private UUID eventId;

}