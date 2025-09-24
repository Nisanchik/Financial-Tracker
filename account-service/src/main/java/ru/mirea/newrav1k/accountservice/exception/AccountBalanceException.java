package ru.mirea.newrav1k.accountservice.exception;

import java.io.Serial;

public class AccountBalanceException extends AccountServiceException {

    @Serial
    private static final long serialVersionUID = -5739537390505360787L;

    public AccountBalanceException(String message) {
        super(message);
    }

    public AccountBalanceException(String message, Object[] args) {
        super(message, args);
    }

}