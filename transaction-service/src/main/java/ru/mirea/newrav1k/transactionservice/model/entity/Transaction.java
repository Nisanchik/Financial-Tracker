package ru.mirea.newrav1k.transactionservice.model.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.mirea.newrav1k.transactionservice.model.enums.TransactionStatus;
import ru.mirea.newrav1k.transactionservice.model.enums.TransactionType;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "t_transactions",
        schema = "transaction_management",
        indexes = {
                @Index(name = "idx_transaction_ids", columnList = "id"),
                @Index(name = "idx_transaction_user_ids", columnList = "userId"),
                @Index(name = "idx_transaction_statuses", columnList = "status")
        }
)
public class Transaction extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "amount", scale = 2, precision = 19, nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @Column(name = "category_id", nullable = false)
    private UUID categoryId;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "description")
    private String description;

    @ElementCollection
    @CollectionTable(
            name = "t_transaction_tags",
            schema = "transaction_management",
            joinColumns = @JoinColumn(name = "transaction_id")
    )
    private List<String> tags = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

}