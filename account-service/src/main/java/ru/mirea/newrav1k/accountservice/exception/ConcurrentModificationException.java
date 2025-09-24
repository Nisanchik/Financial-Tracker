package ru.mirea.newrav1k.accountservice.exception;

import java.io.Serial;

import static ru.mirea.newrav1k.accountservice.utils.MessageCode.CONCURRENT_MODIFICATION;

public class ConcurrentModificationException extends AccountServiceException {

    @Serial
    private static final long serialVersionUID = 4835433077990568532L;

    public ConcurrentModificationException(String message) {
        super(CONCURRENT_MODIFICATION);
    }

    public ConcurrentModificationException(String message, Object[] args) {
        super(CONCURRENT_MODIFICATION, args);
    }

}