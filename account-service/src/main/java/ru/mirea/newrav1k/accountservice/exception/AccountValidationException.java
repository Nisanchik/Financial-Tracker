package ru.mirea.newrav1k.accountservice.exception;

import org.springframework.http.HttpStatus;

import java.io.Serial;

public class AccountValidationException extends AccountServiceException {

    @Serial
    private static final long serialVersionUID = 6578307691099875290L;

    public AccountValidationException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }

}