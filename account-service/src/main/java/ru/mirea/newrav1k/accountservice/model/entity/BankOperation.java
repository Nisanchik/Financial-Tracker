package ru.mirea.newrav1k.accountservice.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "t_bank_operations",
        schema = "account_management",
        indexes = {
                @Index(name = "idx_bank_operation_transaction_ids", columnList = "transactionId")
        }
)
public class BankOperation {

    @Id
    private UUID transactionId;

    @Column(nullable = false)
    private UUID accountId;

    @Column(nullable = false, scale = 2, precision = 19)
    private BigDecimal amount;

}