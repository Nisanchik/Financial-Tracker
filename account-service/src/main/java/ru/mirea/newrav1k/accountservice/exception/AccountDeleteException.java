package ru.mirea.newrav1k.accountservice.exception;

import java.io.Serial;

import static ru.mirea.newrav1k.accountservice.utils.MessageCode.INACTIVE_ACCOUNT_DELETE_FAILURE;

public class AccountDeleteException extends AccountServiceException {

    @Serial
    private static final long serialVersionUID = -542133551679010572L;

    public AccountDeleteException() {
        super(INACTIVE_ACCOUNT_DELETE_FAILURE);
    }

    public AccountDeleteException(String message) {
        super(message);
    }

}