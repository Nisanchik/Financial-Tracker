package ru.mirea.newrav1k.accountservice.exception;

import static ru.mirea.newrav1k.accountservice.utils.MessageCode.ACCOUNT_TYPE_CANNOT_UPDATE;

public class AccountTypeException extends AccountServiceException {

    public AccountTypeException() {
        super(ACCOUNT_TYPE_CANNOT_UPDATE);
    }

}