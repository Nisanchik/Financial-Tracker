package ru.mirea.newrav1k.userservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import static ru.mirea.newrav1k.userservice.utils.MessageCode.CUSTOMER_NOT_FOUND;

@Getter
public class CustomerNotFoundException extends UserServiceException {

    public CustomerNotFoundException() {
        super(CUSTOMER_NOT_FOUND, HttpStatus.NOT_FOUND);
    }

}