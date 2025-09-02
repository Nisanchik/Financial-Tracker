package org.example.transactionservice.model.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.transactionservice.model.enums.TransactionType;

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
                @Index(name = "idx_transaction_user_ids", columnList = "userId")
        }
)
public class Transaction extends BaseEntity {

    private UUID userId;

    private BigDecimal amount;

    private TransactionType type;

    private UUID categoryId;

    private UUID accountId;

    private String description;

    @ElementCollection
    @CollectionTable(
            name = "t_transaction_tags",
            schema = "transaction_management",
            joinColumns = @JoinColumn(name = "transaction_id")
    )
    private List<String> tags = new ArrayList<>();

}