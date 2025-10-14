package ru.mirea.nisanchik.categoryservice.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class CategoryException extends RuntimeException {

    private final HttpStatus status;

    public CategoryException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }

    public CategoryException(String message, Throwable cause, HttpStatus status) {
        super(message, cause);
        this.status = status;
    }
}
