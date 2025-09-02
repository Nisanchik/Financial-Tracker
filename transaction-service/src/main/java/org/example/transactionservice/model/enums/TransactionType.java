package org.example.transactionservice.model.enums;

public enum TransactionType {
    INCOME, EXPENSE;

    public static TransactionType getTransactionType(String transactionType) {
        for (TransactionType type : TransactionType.values()) {
            if (type.toString().equals(transactionType)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid transaction type");
    }

}