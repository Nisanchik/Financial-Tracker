package ru.mirea.newrav1k.accountservice.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.mirea.newrav1k.accountservice.exception.AccountBalanceException;
import ru.mirea.newrav1k.accountservice.exception.AccountValidationException;
import ru.mirea.newrav1k.accountservice.exception.InsufficientCreditException;
import ru.mirea.newrav1k.accountservice.model.enums.AccountType;
import ru.mirea.newrav1k.accountservice.model.enums.Currency;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static ru.mirea.newrav1k.accountservice.utils.MessageCode.ACCOUNT_CREDIT_LIMIT_IS_INSUFFICIENT;
import static ru.mirea.newrav1k.accountservice.utils.MessageCode.ACCOUNT_CREDIT_LIMIT_SPENT;
import static ru.mirea.newrav1k.accountservice.utils.MessageCode.ACCOUNT_HAVE_OUTSTANDING_LOAN_DEBT;
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
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_account_tracker_name", columnNames = {"trackerId", "name"})
        }
)
public class Account extends BaseEntity {

    @Column(name = "tracker_id", nullable = false)
    private UUID trackerId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "balance", scale = 2, precision = 19, nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "credit_limit", scale = 2, precision = 19)
    private BigDecimal creditLimit;

    @Column(name = "credit_debt", scale = 2, precision = 19)
    private BigDecimal creditDebt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private Currency currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType type;

    @Column(name = "active", nullable = false)
    private boolean isActive = true;

    @Column(name = "deleted", nullable = false)
    private boolean isDeleted = false;

    @Column(name = "deleted_at")
    private Instant deletedAt;

    @Column(name = "deleted_reason")
    private String deletedReason;

    public void deposit(BigDecimal amount) {
        validateAmount(amount);

        if (this.type == AccountType.CREDIT_CARD) {
            // Для кредитных карт применяем пополнение к долгу
            applyDepositToCreditDebt(amount);
        } else {
            // Для обычных счетов просто увеличиваем баланс
            this.balance = this.balance.add(amount);
        }
    }

    public void withdraw(BigDecimal amount) {
        validateAmount(amount);
        validateCreditWithdrawal(amount);
        this.balance = this.balance.subtract(amount);
    }

    public void softDelete(String reason) {
        if (this.type == AccountType.CREDIT_CARD) {
            validateCredit(); // Валидация перед удалением кредитного счета
        }
        if (this.balance.signum() != 0 && this.type != AccountType.CREDIT_CARD) {
            throw new AccountBalanceException(BALANCE_NOT_ZERO);
        }
        this.isDeleted = true;
        this.deletedAt = Instant.now();
        this.isActive = false;
        this.deletedReason = reason;
    }

    public void deactivate() {
        if (this.type == AccountType.CREDIT_CARD) {
            validateCredit(); // Валидация перед деактивацией кредитного счета
        } else if (this.balance.signum() != 0) {
            throw new AccountBalanceException(BALANCE_NOT_ZERO);
        }
        this.isActive = false;
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new AccountBalanceException(INVALID_AMOUNT);
        }
    }

    /**
     * Валидация кредитного счета перед удалением или деактивацией
     */
    private void validateCredit() {
        if (this.type != AccountType.CREDIT_CARD) {
            return;
        }
        // 1. Проверка наличия кредитного долга
        if (hasOutstandingCreditDebt()) {
            throw new AccountValidationException(ACCOUNT_HAVE_OUTSTANDING_LOAN_DEBT);
        }
        // 2. Проверка отрицательного баланса (использованный кредит)
        if (isCreditBalanceUsed()) {
            throw new AccountValidationException(ACCOUNT_CREDIT_LIMIT_SPENT);
        }
    }

    /**
     * Проверка наличия непогашенного кредитного долга
     */
    public boolean hasOutstandingCreditDebt() {
        return this.creditDebt != null && this.creditDebt.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Проверка использования кредитного лимита (отрицательный баланс)
     */
    public boolean isCreditBalanceUsed() {
        return this.balance.compareTo(BigDecimal.ZERO) < 0;
    }

    /**
     * Получение суммы использованного кредита
     */
    public BigDecimal getUsedCreditAmount() {
        if (this.balance.compareTo(BigDecimal.ZERO) < 0) {
            return this.balance.abs(); // Возвращаем положительное число
        }
        return BigDecimal.ZERO;
    }

    /**
     * Проверка доступного кредитного лимита
     */
    public BigDecimal getAvailableCredit() {
        if (this.creditLimit == null) {
            return BigDecimal.ZERO;
        }

        // Доступный кредит = лимит - (использованный кредит + долг)
        BigDecimal usedCredit = getUsedCreditAmount();
        BigDecimal totalUsed = usedCredit.add(this.creditDebt != null ? this.creditDebt : BigDecimal.ZERO);

        return this.creditLimit.subtract(totalUsed);
    }

    /**
     * Валидация операции снятия для кредитного счета
     */
    public void validateCreditWithdrawal(BigDecimal amount) {
        if (this.type != AccountType.CREDIT_CARD) {
            return;
        }
        validateAmount(amount);
        // Для кредитного счета проверяем доступный лимит
        BigDecimal availableCredit = getAvailableCredit();
        if (amount.compareTo(availableCredit) > 0) {
            throw new InsufficientCreditException(ACCOUNT_CREDIT_LIMIT_IS_INSUFFICIENT);
        }
    }

    /**
     * Автоматическое применение пополнения к погашению кредитного долга
     */
    private void applyDepositToCreditDebt(BigDecimal amount) {
        BigDecimal remainingAmount = amount;

        // 1. Сначала погашаем отрицательный баланс (использованный кредит)
        if (isCreditBalanceUsed()) {
            BigDecimal usedCredit = getUsedCreditAmount();
            BigDecimal creditRepayment = usedCredit.min(remainingAmount);

            this.balance = this.balance.add(creditRepayment);
            remainingAmount = remainingAmount.subtract(creditRepayment);
        }

        // 2. Если остались средства после погашения отрицательного баланса,
        //    погашаем основной кредитный долг
        if (remainingAmount.compareTo(BigDecimal.ZERO) > 0 && hasOutstandingCreditDebt()) {
            BigDecimal debtRepayment = this.creditDebt.min(remainingAmount);
            this.creditDebt = this.creditDebt.subtract(debtRepayment);
            remainingAmount = remainingAmount.subtract(debtRepayment);
        }

        // 3. Если после всего еще остались средства, добавляем к балансу
        if (remainingAmount.compareTo(BigDecimal.ZERO) > 0) {
            this.balance = this.balance.add(remainingAmount);
        }
    }

}