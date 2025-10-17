package ru.mirea.nisanchik.categoryservice.exception;

import org.springframework.http.HttpStatus;

import java.io.Serial;

import static ru.mirea.nisanchik.categoryservice.utils.MessageCode.CATEGORY_ACCESS_DENIED;

public class CategoryAccessDeniedException extends CategoryServiceException {

    @Serial
    private static final long serialVersionUID = 3089249313723108167L;

    public CategoryAccessDeniedException() {
        super(CATEGORY_ACCESS_DENIED, HttpStatus.UNAUTHORIZED);
    }

}