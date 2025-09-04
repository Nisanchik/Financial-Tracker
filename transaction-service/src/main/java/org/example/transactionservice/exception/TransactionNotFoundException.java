package org.example.transactionservice.exception;

public class TransactionNotFoundException extends RuntimeException {

    public TransactionNotFoundException() {
        super();
    }

    public TransactionNotFoundException(String message) {
        super(message);
    }

    public TransactionNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public TransactionNotFoundException(Throwable cause) {
        super(cause);
    }

    protected TransactionNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}