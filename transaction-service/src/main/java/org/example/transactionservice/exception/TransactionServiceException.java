package org.example.transactionservice.exception;

import lombok.Getter;

@Getter
public class TransactionServiceException extends RuntimeException {

    public final String messageCode;

    public final transient Object[] args;

    public TransactionServiceException(String message) {
        this.messageCode = message;
        this.args = new Object[0];
    }

    public TransactionServiceException(String message, Object ... args) {
        this.messageCode = message;
        this.args = args;
    }

}