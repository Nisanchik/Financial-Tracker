package ru.mirea.newrav1k.accountservice.exception;

import java.io.Serial;

import static ru.mirea.newrav1k.accountservice.utils.MessageCode.ACCOUNT_NAME_ALREADY_EXIST;

public class AccountAlreadyExistException extends AccountServiceException {

    @Serial
    private static final long serialVersionUID = -3892083169817094835L;

    public AccountAlreadyExistException() {
        super(ACCOUNT_NAME_ALREADY_EXIST);
    }

}