package ru.mirea.newrav1k.accountservice.model.enums;

public enum AccountType {
    CASH, DEBIT_CARD, CREDIT_CARD, SAVINGS_CARD;

    public static AccountType findAccountType(String accountType) {
        for (AccountType type : AccountType.values()) {
            if (type.toString().equalsIgnoreCase(accountType)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid account type: " + accountType);
    }

}