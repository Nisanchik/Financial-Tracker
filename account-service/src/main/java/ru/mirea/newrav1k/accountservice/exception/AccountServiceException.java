package ru.mirea.newrav1k.accountservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.io.Serial;

@Getter
public class AccountServiceException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -1656310440954290228L;

    private final HttpStatus httpStatus;

    public AccountServiceException(String message, HttpStatus httpStatus) {
        super(message);
        this.httpStatus = httpStatus;
    }

    public AccountServiceException(String message, Throwable cause, HttpStatus httpStatus) {
        super(message, cause);
        this.httpStatus = httpStatus;
    }

}