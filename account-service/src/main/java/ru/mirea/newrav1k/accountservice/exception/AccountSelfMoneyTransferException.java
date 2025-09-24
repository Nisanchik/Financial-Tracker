package ru.mirea.newrav1k.accountservice.exception;

import java.io.Serial;

import static ru.mirea.newrav1k.accountservice.utils.MessageCode.ACCOUNT_SELF_MONEY_TRANSFER_FAILURE;

public class AccountSelfMoneyTransferException extends AccountServiceException {

    @Serial
    private static final long serialVersionUID = -6821055287699845754L;

    public AccountSelfMoneyTransferException() {
        super(ACCOUNT_SELF_MONEY_TRANSFER_FAILURE);
    }

    public AccountSelfMoneyTransferException(String message) {
        super(message);
    }

}