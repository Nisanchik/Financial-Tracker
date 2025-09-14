package ru.mirea.newrav1k.userservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import static ru.mirea.newrav1k.userservice.utils.MessageCode.PASSWORD_MISMATCH;

@Getter
public class PasswordMismatchException extends UserServiceException {

    public PasswordMismatchException() {
        super(PASSWORD_MISMATCH, HttpStatus.UNAUTHORIZED);
    }

}