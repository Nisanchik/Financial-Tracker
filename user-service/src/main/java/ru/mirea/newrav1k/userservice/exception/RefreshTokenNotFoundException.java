package ru.mirea.newrav1k.userservice.exception;

import org.springframework.http.HttpStatus;

import static ru.mirea.newrav1k.userservice.utils.MessageCode.REFRESH_TOKEN_NOT_FOUND;

public class RefreshTokenNotFoundException extends UserServiceException {

    public RefreshTokenNotFoundException() {
        super(REFRESH_TOKEN_NOT_FOUND, HttpStatus.NOT_FOUND);
    }

}