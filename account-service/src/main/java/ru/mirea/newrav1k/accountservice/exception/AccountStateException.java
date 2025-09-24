package ru.mirea.newrav1k.accountservice.exception;

import java.io.Serial;

import static ru.mirea.newrav1k.accountservice.utils.MessageCode.ACCOUNT_INACTIVE;

public class AccountStateException extends AccountServiceException {

    @Serial
    private static final long serialVersionUID = -3613089270872112900L;

    public AccountStateException() {
        super(ACCOUNT_INACTIVE);
    }

}