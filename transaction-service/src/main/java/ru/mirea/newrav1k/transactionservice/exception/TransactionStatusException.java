package ru.mirea.newrav1k.transactionservice.exception;

import org.springframework.http.HttpStatus;

import static ru.mirea.newrav1k.transactionservice.utils.MessageCode.TRANSACTION_ALREADY_COMPLETED;

public class TransactionStatusException extends TransactionServiceException {

    public TransactionStatusException() {
        super(TRANSACTION_ALREADY_COMPLETED, HttpStatus.CONFLICT);
    }

}