package ru.mirea.newrav1k.userservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class UserServiceException extends RuntimeException {

    private final HttpStatus status;

    public UserServiceException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public UserServiceException(String message, Throwable cause, HttpStatus status) {
        super(message, cause);
        this.status = status;
    }

}