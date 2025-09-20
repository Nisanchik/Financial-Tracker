package ru.mirea.newrav1k.accountservice.exception;

import static ru.mirea.newrav1k.accountservice.utils.MessageCode.ACCOUNT_ACCESS_DENIED;

public class AccountAccessDeniedException extends AccountServiceException {

    public AccountAccessDeniedException() {
        super(ACCOUNT_ACCESS_DENIED);
    }

}