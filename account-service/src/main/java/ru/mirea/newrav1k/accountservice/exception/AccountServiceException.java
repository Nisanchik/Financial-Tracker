package ru.mirea.newrav1k.accountservice.exception;

import lombok.Getter;

@Getter
public class AccountServiceException extends RuntimeException {

    private final String messageCode;

    private final transient Object[] args;

    public AccountServiceException(String message) {
        this.messageCode = message;
        this.args = new Object[0];
    }

    public AccountServiceException(String message, Object[] args) {
        this.messageCode = message;
        this.args = args;
    }

}