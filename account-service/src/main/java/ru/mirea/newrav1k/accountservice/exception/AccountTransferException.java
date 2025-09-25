package ru.mirea.newrav1k.accountservice.exception;

import org.springframework.http.HttpStatus;

import java.io.Serial;

import static ru.mirea.newrav1k.accountservice.utils.MessageCode.ACCOUNT_SELF_MONEY_TRANSFER_FAILURE;

public class AccountTransferException extends AccountServiceException {

    @Serial
    private static final long serialVersionUID = -6821055287699845754L;

    public AccountTransferException() {
        super(ACCOUNT_SELF_MONEY_TRANSFER_FAILURE, HttpStatus.BAD_REQUEST);
    }

}