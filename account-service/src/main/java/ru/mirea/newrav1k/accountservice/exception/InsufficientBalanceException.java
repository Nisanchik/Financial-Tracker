package ru.mirea.newrav1k.accountservice.exception;

import java.io.Serial;

import static ru.mirea.newrav1k.accountservice.utils.MessageCode.INSUFFICIENT_BALANCE;

public class InsufficientBalanceException extends AccountServiceException {

    @Serial
    private static final long serialVersionUID = -1398555137357926582L;

    public InsufficientBalanceException() {
        super(INSUFFICIENT_BALANCE);
    }

    public InsufficientBalanceException(Object[] args) {
        super(INSUFFICIENT_BALANCE, args);
    }

}