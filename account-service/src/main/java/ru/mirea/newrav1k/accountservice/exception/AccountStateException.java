package ru.mirea.newrav1k.accountservice.exception;

import static ru.mirea.newrav1k.accountservice.utils.MessageCode.ACCOUNT_INACTIVE;

public class AccountStateException extends AccountServiceException {

    public AccountStateException() {
        super(ACCOUNT_INACTIVE);
    }

}