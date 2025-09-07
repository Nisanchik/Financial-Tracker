package ru.mirea.newrav1k.transactionservice.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MessageCode {

    public static final String TRANSACTION_NOT_FOUND = "transaction.not.found";

    public static final String TRANSACTION_UPDATE_FAILED = "error.transaction_update_failed";

}