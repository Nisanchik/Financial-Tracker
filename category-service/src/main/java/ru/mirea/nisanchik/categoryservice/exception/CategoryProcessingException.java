package ru.mirea.nisanchik.categoryservice.exception;

import org.springframework.http.HttpStatus;

import static ru.mirea.nisanchik.categoryservice.utils.MessageCode.CATEGORY_PROCESSING_FAILED;

public class CategoryProcessingException extends CategoryException {

    public CategoryProcessingException() {
        super(CATEGORY_PROCESSING_FAILED, HttpStatus.BAD_REQUEST);
    }
}
