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
import lombok.extern.slf4j.Slf4j;
import ru.mirea.newrav1k.accountservice.exception.AccountBalanceException;
import ru.mirea.newrav1k.accountservice.exception.AccountStateException;
import ru.mirea.newrav1k.accountservice.exception.AccountValidationException;
import ru.mirea.newrav1k.accountservice.model.enums.AccountType;
import ru.mirea.newrav1k.accountservice.model.enums.Currency;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static ru.mirea.newrav1k.accountservice.utils.MessageCode.ACCOUNT_CANNOT_ACTIVATE_BECAUSE_DELETED;
import static ru.mirea.newrav1k.accountservice.utils.MessageCode.ACCOUNT_CREDIT_LIMIT_IS_INSUFFICIENT;
import static ru.mirea.newrav1k.accountservice.utils.MessageCode.ACCOUNT_DELETED;
import static ru.mirea.newrav1k.accountservice.utils.MessageCode.ACCOUNT_HAVE_OUTSTANDING_LOAN_DEBT;
import static ru.mirea.newrav1k.accountservice.utils.MessageCode.ACCOUNT_INACTIVE;
import static ru.mirea.newrav1k.accountservice.utils.MessageCode.BALANCE_NOT_ZERO;
import static ru.mirea.newrav1k.accountservice.utils.MessageCode.INSUFFICIENT_BALANCE;
import static ru.mirea.newrav1k.accountservice.utils.MessageCode.INVALID_AMOUNT;

@Slf4j
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
    private BigDecimal creditLimit = BigDecimal.ZERO;

    @Column(name = "credit_debt", scale = 2, precision = 19)
    private BigDecimal creditDebt = BigDecimal.ZERO;

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

    // ===== BASIC VALIDATION =====
    public void validateAccountState() {
        if (this.isDeleted) {
            throw new AccountStateException(ACCOUNT_DELETED);
        }
        if (!this.isActive) {
            throw new AccountStateException(ACCOUNT_INACTIVE);
        }
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(INVALID_AMOUNT);
        }
    }

    private void validateOperationAllowed() {
        validateAccountState();
        // Дополнительные проверки в зависимости от типа счета
    }

    // ===== BALANCE OPERATIONS =====

    /**
     * Универсальный метод пополнения счета
     * Для кредитных карт: сначала погашаем использованный кредит, потом долг, потом увеличиваем баланс
     */
    public void deposit(BigDecimal amount) {
        validateOperationAllowed();
        validateAmount(amount);

        if (this.type == AccountType.CREDIT_CARD) {
            processCreditCardDeposit(amount);
        } else {
            // Для обычных счетов - простое пополнение
            this.balance = this.balance.add(amount);
        }

        logDepositOperation(amount);
    }

    /**
     * Универсальный метод снятия средств
     */
    public void withdraw(BigDecimal amount) {
        validateOperationAllowed();
        validateAmount(amount);

        if (!canWithdraw(amount)) {
            throw new AccountBalanceException(
                    this.type == AccountType.CREDIT_CARD ? ACCOUNT_CREDIT_LIMIT_IS_INSUFFICIENT : INSUFFICIENT_BALANCE
            );
        }

        this.balance = this.balance.subtract(amount);
        logWithdrawalOperation(amount);
    }

    /**
     * Проверка возможности снятия средств
     */
    private boolean canWithdraw(BigDecimal amount) {
        validateAmount(amount);

        if (this.type == AccountType.CREDIT_CARD) {
            return calculateAvailableCredit().compareTo(amount) >= 0;
        } else {
            return this.balance.compareTo(amount) >= 0;
        }
    }

    // ===== CREDIT CARD SPECIFIC LOGIC =====

    /**
     * Логика пополнения для кредитной карты с приоритетом погашения
     */
    private void processCreditCardDeposit(BigDecimal amount) {
        BigDecimal remainingAmount = amount;

        // 1. Погашение использованного кредитного лимита (отрицательный баланс)
        if (this.balance.compareTo(BigDecimal.ZERO) < 0) {
            BigDecimal creditUsed = this.balance.abs();
            BigDecimal repaymentAmount = creditUsed.min(remainingAmount);

            this.balance = this.balance.add(repaymentAmount);
            remainingAmount = remainingAmount.subtract(repaymentAmount);
        }

        // 2. Погашение основного кредитного долга
        if (remainingAmount.compareTo(BigDecimal.ZERO) > 0 && this.creditDebt.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal debtRepayment = this.creditDebt.min(remainingAmount);
            this.creditDebt = this.creditDebt.subtract(debtRepayment);
            remainingAmount = remainingAmount.subtract(debtRepayment);
        }

        // 3. Остаток средств добавляем к балансу
        if (remainingAmount.compareTo(BigDecimal.ZERO) > 0) {
            this.balance = this.balance.add(remainingAmount);
        }
    }

    /**
     * Расчет доступного кредитного лимита
     */
    public BigDecimal calculateAvailableCredit() {
        if (this.type != AccountType.CREDIT_CARD) {
            return BigDecimal.ZERO;
        }

        // Доступный кредит = общий лимит - (использованный кредит + основной долг)
        BigDecimal usedCredit = this.balance.compareTo(BigDecimal.ZERO) < 0 ? this.balance.abs() : BigDecimal.ZERO;
        BigDecimal totalUsed = usedCredit.add(this.creditDebt);

        return this.creditLimit.subtract(totalUsed).max(BigDecimal.ZERO);
    }

    /**
     * Получение общей задолженности по кредитной карте
     */
    public BigDecimal getTotalDebt() {
        if (this.type != AccountType.CREDIT_CARD) {
            return BigDecimal.ZERO;
        }

        BigDecimal usedCredit = this.balance.compareTo(BigDecimal.ZERO) < 0 ? this.balance.abs() : BigDecimal.ZERO;
        return usedCredit.add(this.creditDebt);
    }

    /**
     * Проверка наличия задолженности
     */
    public boolean hasDebt() {
        return getTotalDebt().compareTo(BigDecimal.ZERO) > 0;
    }

    // ===== ACCOUNT MANAGEMENT =====

    /**
     * Мягкое удаление счета с валидацией
     */
    public void softDelete() {
        if (this.isDeleted) {
            return; // Уже удален
        }

        validateAccountForDeletion();

        this.isDeleted = true;
        this.deletedAt = Instant.now();
        this.isActive = false;
    }

    /**
     * Деактивация счета
     */
    public void deactivate() {
        if (!this.isActive) {
            return; // Уже деактивирован
        }

        validateAccountForDeactivation();
        this.isActive = false;
    }

    /**
     * Активация счета
     */
    public void activate() {
        if (this.isDeleted) {
            throw new AccountStateException(ACCOUNT_CANNOT_ACTIVATE_BECAUSE_DELETED);
        }
        this.isActive = true;
    }

    // ===== VALIDATION METHODS =====

    public void validateAccountForDeletion() {
        if (this.type == AccountType.CREDIT_CARD) {
            validateCreditCardForDeletion();
        } else {
            validateRegularAccountForDeletion();
        }
    }

    public void validateAccountForDeactivation() {
        if (this.type == AccountType.CREDIT_CARD) {
            validateCreditCardForDeactivation();
        } else {
            validateRegularAccountForDeactivation();
        }
    }

    private void validateCreditCardForDeletion() {
        if (hasDebt()) {
            throw new AccountValidationException(ACCOUNT_HAVE_OUTSTANDING_LOAN_DEBT);
        }

        // Для кредитной карты разрешаем удаление с положительным балансом
        // (деньги будут возвращены владельцу через отдельный процесс)
        if (this.balance.compareTo(BigDecimal.ZERO) > 0) {
            log.warn("Deleting credit card with positive balance: {}", this.balance);
        }
    }

    private void validateCreditCardForDeactivation() {
        if (hasDebt()) {
            throw new AccountValidationException(ACCOUNT_HAVE_OUTSTANDING_LOAN_DEBT);
        }
    }

    private void validateRegularAccountForDeletion() {
        if (balance.compareTo(BigDecimal.ZERO) != 0) {
            throw new AccountBalanceException(BALANCE_NOT_ZERO);
        }
    }

    private void validateRegularAccountForDeactivation() {
        if (balance.compareTo(BigDecimal.ZERO) != 0) {
            throw new AccountBalanceException(BALANCE_NOT_ZERO);
        }
    }

    // ===== BUSINESS LOGIC QUERIES =====

    public boolean canTransferFrom() {
        if (!this.isActive || this.isDeleted) {
            return false;
        }

        if (this.type == AccountType.CREDIT_CARD) {
            return calculateAvailableCredit().compareTo(BigDecimal.ZERO) > 0;
        } else {
            return balance.compareTo(BigDecimal.ZERO) > 0;
        }
    }

    public boolean canTransferTo() {
        return this.isActive && !this.isDeleted;
    }

    public BigDecimal getMaxWithdrawalAmount() {
        if (this.type == AccountType.CREDIT_CARD) {
            return calculateAvailableCredit();
        } else {
            return balance;
        }
    }

    // ===== LOGGING =====

    private void logDepositOperation(BigDecimal amount) {
        log.info("Deposit completed: account={}, type={}, amount={}, newBalance={}, creditDebt={}",
                super.getId(), this.type, amount, this.balance, this.creditDebt);
    }

    private void logWithdrawalOperation(BigDecimal amount) {
        log.info("Withdrawal completed: account={}, type={}, amount={}, newBalance={}",
                super.getId(), this.type, amount, this.balance);
    }

}