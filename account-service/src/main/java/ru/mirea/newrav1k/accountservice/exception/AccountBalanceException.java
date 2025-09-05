package ru.mirea.newrav1k.accountservice.exception;

public class AccountBalanceException extends AccountServiceException {

    public AccountBalanceException(String message) {
        super(message);
    }

    public AccountBalanceException(String message, Object[] args) {
        super(message, args);
    }

}