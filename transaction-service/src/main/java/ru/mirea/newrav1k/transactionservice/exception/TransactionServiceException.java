package ru.mirea.newrav1k.transactionservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class TransactionServiceException extends RuntimeException {

    private final HttpStatus status;

    public TransactionServiceException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public TransactionServiceException(String message, Throwable cause, HttpStatus status) {
        super(message, cause);
        this.status = status;
    }

}