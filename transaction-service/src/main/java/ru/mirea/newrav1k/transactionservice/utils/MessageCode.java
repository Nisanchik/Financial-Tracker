package ru.mirea.newrav1k.transactionservice.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MessageCode {

    public static final String TRANSACTION_NOT_FOUND = "error.transaction_not_found";

    public static final String TRANSACTION_UPDATE_FAILED = "error.transaction_update_failed";

    public static final String TRANSACTION_CREATE_FAILED = "error.transaction_create_failed";

    public static final String TRANSACTION_COMPENSATE_FAILED = "error.transaction_compensate_failed";

    public static final String TRANSACTION_PROCESSING_FAILED = "error.transaction_processing_failed";

    public static final String TRANSACTION_ALREADY_COMPLETED = "error.transaction_already_completed";

    public static final String TRANSACTION_ACCESS_DENIED = "error.transaction_access_denied";

}