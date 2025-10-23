package ru.mirea.nisanchik.categoryservice.exception;

import org.springframework.http.HttpStatus;

import java.io.Serial;

import static ru.mirea.nisanchik.categoryservice.utils.MessageCode.CATEGORY_NOT_FOUND;

public class CategoryNotFoundException extends CategoryServiceException {

    @Serial
    private static final long serialVersionUID = 464592616766196596L;

    public CategoryNotFoundException() {
        super(CATEGORY_NOT_FOUND, HttpStatus.NOT_FOUND);
    }

}