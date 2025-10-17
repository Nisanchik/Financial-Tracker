package ru.mirea.nisanchik.categoryservice.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MessageCode {

    public static final String CATEGORY_NOT_FOUND = "error.category_not_found";

    public static final String CATEGORY_UPDATE_FAILED = "error.category_update_failed";

    public static final String CATEGORY_CREATE_FAILED = "error.category_create_failed";

    public static final String CATEGORY_COMPENSATE_FAILED = "error.category_compensate_failed";

    public static final String CATEGORY_PROCESSING_FAILED = "error.category_processing_failed";

    public static final String CATEGORY_ALREADY_COMPLETED = "error.category_already_completed";

    public static final String CATEGORY_ACCESS_DENIED = "error.category_access_denied";
    public static final String CATEGORY_TYPE_CHANGE_FAILED = "error.category_type_change_failed";

}