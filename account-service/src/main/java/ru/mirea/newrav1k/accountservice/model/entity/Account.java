package ru.mirea.newrav1k.accountservice.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.mirea.newrav1k.accountservice.exception.AccountBalanceException;
import ru.mirea.newrav1k.accountservice.exception.InsufficientBalanceException;
import ru.mirea.newrav1k.accountservice.model.enums.AccountType;
import ru.mirea.newrav1k.accountservice.model.enums.Currency;

import java.math.BigDecimal;
import java.util.UUID;

import static ru.mirea.newrav1k.accountservice.utils.MessageCode.BALANCE_NOT_ZERO;
import static ru.mirea.newrav1k.accountservice.utils.MessageCode.INVALID_AMOUNT;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "t_accounts",
        schema = "account_management",
        indexes = {
                @Index(name = "idx_account_ids", columnList = "id"),
                @Index(name = "idx_account_tracker_ids", columnList = "trackerId")
        }
)
public class Account extends BaseEntity {

    @Column(name = "tracker_id", nullable = false)
    private UUID trackerId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "balance", scale = 2, precision = 19, nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private Currency currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType type;

    @Column(name = "active", nullable = false)
    private boolean isActive = true;

    public void deposit(BigDecimal amount) {
        validateAmount(amount);
        this.balance = this.balance.add(amount);
    }

    public void withdraw(BigDecimal amount) {
        validateAmount(amount);
        if (this.balance.compareTo(amount) < 0) {
            throw new InsufficientBalanceException();
        }
        this.balance = this.balance.subtract(amount);
    }

    public void deactivate() {
        if (this.balance.signum() != 0) {
            throw new AccountBalanceException(BALANCE_NOT_ZERO);
        }
        this.isActive = false;
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new AccountBalanceException(INVALID_AMOUNT);
        }
    }

}