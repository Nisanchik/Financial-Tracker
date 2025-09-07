package ru.mirea.newrav1k.transactionservice.exception;

import static ru.mirea.newrav1k.transactionservice.utils.MessageCode.TRANSACTION_NOT_FOUND;

public class TransactionNotFoundException extends TransactionServiceException {

    public TransactionNotFoundException() {
        super(TRANSACTION_NOT_FOUND);
    }

    public TransactionNotFoundException(Object... args) {
        super(TRANSACTION_NOT_FOUND, args);
    }

}