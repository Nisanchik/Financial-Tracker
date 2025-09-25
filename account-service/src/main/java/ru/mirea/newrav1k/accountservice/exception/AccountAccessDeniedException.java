package ru.mirea.newrav1k.accountservice.exception;

import org.springframework.http.HttpStatus;

import java.io.Serial;

import static ru.mirea.newrav1k.accountservice.utils.MessageCode.ACCOUNT_ACCESS_DENIED;

public class AccountAccessDeniedException extends AccountServiceException {

    @Serial
    private static final long serialVersionUID = 2311666664767059405L;

    public AccountAccessDeniedException() {
        super(ACCOUNT_ACCESS_DENIED, HttpStatus.UNAUTHORIZED);
    }

}