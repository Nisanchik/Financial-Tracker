package ru.mirea.nisanchik.categoryservice.model.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import ru.mirea.nisanchik.categoryservice.model.enums.CategoryType;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "t_categories",
        schema = "category_management",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_category_name", columnNames = {"trackerId", "type", "name"})
        },
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

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @CreationTimestamp
    private Instant createdAt;

    @UpdateTimestamp
    private Instant updatedAt;
}
