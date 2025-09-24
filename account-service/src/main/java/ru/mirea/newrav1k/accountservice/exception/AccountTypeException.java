package ru.mirea.newrav1k.accountservice.exception;

import java.io.Serial;

import static ru.mirea.newrav1k.accountservice.utils.MessageCode.ACCOUNT_TYPE_CANNOT_UPDATE;

public class AccountTypeException extends AccountServiceException {

    @Serial
    private static final long serialVersionUID = 7387628024883850577L;

    public AccountTypeException() {
        super(ACCOUNT_TYPE_CANNOT_UPDATE);
    }

}