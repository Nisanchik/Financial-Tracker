package ru.mirea.newrav1k.accountservice.exception;

import java.io.Serial;

import static ru.mirea.newrav1k.accountservice.utils.MessageCode.ACCOUNT_NOT_FOUND;

public class AccountNotFoundException extends AccountServiceException {

    @Serial
    private static final long serialVersionUID = 5240958547277863696L;

    public AccountNotFoundException() {
        super(ACCOUNT_NOT_FOUND);
    }

    public AccountNotFoundException(Object[] args) {
        super(ACCOUNT_NOT_FOUND, args);
    }

}