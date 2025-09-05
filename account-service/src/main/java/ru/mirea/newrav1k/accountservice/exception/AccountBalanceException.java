package ru.mirea.newrav1k.accountservice.exception;

public class AccountBalanceException extends RuntimeException {

    public AccountBalanceException() {
        super();
    }

    public AccountBalanceException(String message) {
        super(message);
    }

    public AccountBalanceException(String message, Throwable cause) {
        super(message, cause);
    }

    public AccountBalanceException(Throwable cause) {
        super(cause);
    }

    protected AccountBalanceException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}