package ru.mirea.newrav1k.userservice.exception;

import org.springframework.http.HttpStatus;

import static ru.mirea.newrav1k.userservice.utils.MessageCode.CUSTOMER_ALREADY_EXISTS;

public class CustomerAlreadyExistsException extends UserServiceException {

    public CustomerAlreadyExistsException() {
        super(CUSTOMER_ALREADY_EXISTS, HttpStatus.CONFLICT);
    }

}