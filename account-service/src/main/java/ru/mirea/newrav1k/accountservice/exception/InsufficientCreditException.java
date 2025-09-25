package ru.mirea.newrav1k.accountservice.exception;

import org.springframework.http.HttpStatus;

import java.io.Serial;

public class InsufficientCreditException extends AccountServiceException {

    @Serial
    private static final long serialVersionUID = 1859398923572395072L;

    public InsufficientCreditException(String message) {
        super(message, HttpStatus.CONFLICT);
    }

}