package ru.mirea.newrav1k.userservice.exception;

import org.springframework.http.HttpStatus;

import static ru.mirea.newrav1k.userservice.utils.MessageCode.JWT_EXPIRED;

public class JwtExpiredException extends UserServiceException {

    public JwtExpiredException() {
        super(JWT_EXPIRED, HttpStatus.CONFLICT);
    }

}