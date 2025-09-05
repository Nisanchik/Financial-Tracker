package ru.mirea.newrav1k.accountservice.exception;

import static ru.mirea.newrav1k.accountservice.utils.MessageCode.CONCURRENT_MODIFICATION;

public class ConcurrentModificationException extends AccountServiceException {

    public ConcurrentModificationException(String message) {
        super(CONCURRENT_MODIFICATION);
    }

    public ConcurrentModificationException(String message, Object[] args) {
        super(CONCURRENT_MODIFICATION, args);
    }

}