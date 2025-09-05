package ru.mirea.newrav1k.accountservice.exception;

import static ru.mirea.newrav1k.accountservice.utils.MessageCode.ACCOUNT_NOT_FOUND;

public class AccountNotFoundException extends AccountServiceException {

    public AccountNotFoundException() {
        super(ACCOUNT_NOT_FOUND);
    }

    public AccountNotFoundException(Object[] args) {
        super(ACCOUNT_NOT_FOUND, args);
    }

}