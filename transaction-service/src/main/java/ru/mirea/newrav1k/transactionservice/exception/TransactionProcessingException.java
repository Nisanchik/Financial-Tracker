package ru.mirea.newrav1k.transactionservice.exception;

import static ru.mirea.newrav1k.transactionservice.utils.MessageCode.TRANSACTION_PROCESSING_FAILED;

public class TransactionProcessingException extends TransactionServiceException {

    public TransactionProcessingException() {
        super(TRANSACTION_PROCESSING_FAILED);
    }

}