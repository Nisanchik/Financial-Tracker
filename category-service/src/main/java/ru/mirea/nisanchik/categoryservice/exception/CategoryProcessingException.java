package ru.mirea.nisanchik.categoryservice.exception;

import org.springframework.http.HttpStatus;

import java.io.Serial;

import static ru.mirea.nisanchik.categoryservice.utils.MessageCode.CATEGORY_PROCESSING_FAILED;

public class CategoryProcessingException extends CategoryServiceException {

    @Serial
    private static final long serialVersionUID = 3590521230185225535L;

    public CategoryProcessingException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }

}