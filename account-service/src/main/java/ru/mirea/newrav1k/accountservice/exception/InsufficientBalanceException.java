package ru.mirea.newrav1k.accountservice.exception;

import static ru.mirea.newrav1k.accountservice.utils.MessageCode.INSUFFICIENT_BALANCE;

public class InsufficientBalanceException extends AccountServiceException {

    public InsufficientBalanceException() {
        super(INSUFFICIENT_BALANCE);
    }

    public InsufficientBalanceException(Object[] args) {
        super(INSUFFICIENT_BALANCE, args);
    }

}