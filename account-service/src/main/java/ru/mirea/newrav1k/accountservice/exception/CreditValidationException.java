package ru.mirea.newrav1k.accountservice.exception;

import java.io.Serial;

public class CreditValidationException extends AccountServiceException {

    @Serial
    private static final long serialVersionUID = -3248705369187093526L;

    public CreditValidationException(String message) {
        super(message);
    }

}