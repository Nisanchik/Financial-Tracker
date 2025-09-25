package ru.mirea.newrav1k.accountservice.exception;

import org.springframework.http.HttpStatus;

import java.io.Serial;

public class AccountDuplicateException extends AccountServiceException {

    @Serial
    private static final long serialVersionUID = -3892083169817094835L;

    public AccountDuplicateException(String message) {
        super(message, HttpStatus.CONFLICT);
    }

}