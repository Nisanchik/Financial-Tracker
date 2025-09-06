package org.example.transactionservice.exception;

import static org.example.transactionservice.utils.MessageCode.TRANSACTION_NOT_FOUND;

public class TransactionNotFoundException extends TransactionServiceException {

    public TransactionNotFoundException() {
        super(TRANSACTION_NOT_FOUND);
    }

    public TransactionNotFoundException(Object... args) {
        super(TRANSACTION_NOT_FOUND, args);
    }

}