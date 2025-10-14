package ru.mirea.nisanchik.categoryservice.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import ru.mirea.nisanchik.categoryservice.model.enums.CategoryType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "t_categories",
        schema = "categories_management",
        indexes = {
                @Index(name = "idx_category_ids", columnList = "id"),
                @Index(name = "idx_category_types", columnList = "type"),
                @Index(name = "idx_category_created_ats", columnList = "createdAt")
        }
)
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;


    @Column(name = "tracker_id", nullable = false)
    private UUID trackerId;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private CategoryType type;

    @Column(name = "is_system", nullable = false)
    private Boolean isSystem = false;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
