package ru.mirea.newrav1k.accountservice.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MessageCode {

    public static final String ACCOUNT_NOT_FOUND = "error.account_not_found";

    public static final String INSUFFICIENT_BALANCE = "error.insufficient_balance";

    public static final String CONCURRENT_MODIFICATION = "error.concurrent_modification";

    public static final String INVALID_AMOUNT = "error.invalid_amount";

    public static final String GENERIC_ERROR = "error.generic";

    public static final String RETRY_EXHAUSTED = "error.retry_exhausted";

    public static final String BALANCE_NOT_ZERO = "error.account_not_zero_balance";

}