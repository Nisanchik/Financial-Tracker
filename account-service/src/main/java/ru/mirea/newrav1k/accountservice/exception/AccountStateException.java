package ru.mirea.newrav1k.accountservice.exception;

import org.springframework.http.HttpStatus;

import java.io.Serial;

public class AccountStateException extends AccountServiceException {

    @Serial
    private static final long serialVersionUID = 4588567844303704459L;

    public AccountStateException(String message) {
        super(message, HttpStatus.CONFLICT);
    }

}