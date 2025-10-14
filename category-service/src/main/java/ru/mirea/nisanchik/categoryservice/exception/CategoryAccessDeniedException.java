package ru.mirea.nisanchik.categoryservice.exception;

import org.springframework.http.HttpStatus;

import static ru.mirea.nisanchik.categoryservice.utils.MessageCode.CATEGORY_ACCESS_DENIED;

public class CategoryAccessDeniedException extends CategoryException {

    public CategoryAccessDeniedException() {
        super(CATEGORY_ACCESS_DENIED, HttpStatus.UNAUTHORIZED);
    }
}
