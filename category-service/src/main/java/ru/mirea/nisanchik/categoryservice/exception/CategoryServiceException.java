package ru.mirea.nisanchik.categoryservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.io.Serial;

@Getter
public class CategoryServiceException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = -5827826710312956252L;

    private final HttpStatus status;

    public CategoryServiceException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public CategoryServiceException(String message, Throwable cause, HttpStatus status) {
        super(message, cause);
        this.status = status;
    }

}