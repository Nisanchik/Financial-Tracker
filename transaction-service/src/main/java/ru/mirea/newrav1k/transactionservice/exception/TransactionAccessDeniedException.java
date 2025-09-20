package ru.mirea.newrav1k.transactionservice.exception;

import org.springframework.http.HttpStatus;

import static ru.mirea.newrav1k.transactionservice.utils.MessageCode.TRANSACTION_ACCESS_DENIED;

public class TransactionAccessDeniedException extends TransactionServiceException {

    public TransactionAccessDeniedException() {
        super(TRANSACTION_ACCESS_DENIED, HttpStatus.UNAUTHORIZED);
    }

}