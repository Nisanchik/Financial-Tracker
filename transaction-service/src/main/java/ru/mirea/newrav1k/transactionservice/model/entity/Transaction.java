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
                @Index(name = "idx_transaction_tracker_ids", columnList = "trackerId"),
                @Index(name = "idx_transaction_statuses", columnList = "status")
        }
)
public class Transaction extends BaseEntity {

    @Column(name = "tracker_id", nullable = false)
    private UUID trackerId;

    @Column(name = "amount", scale = 2, precision = 19, nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private TransactionType type;

    @Column(name = "category_id", nullable = false)
    private UUID categoryId;

    // TODO: изменить поле для идентификатора аккаунта отправителя
    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    // TODO: добавить поле для идентификатора аккаунта получателя

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
    @Column(name = "status", nullable = false)
    private TransactionStatus status;

}