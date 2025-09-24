package ru.mirea.newrav1k.accountservice.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MessageCode {

    public static final String ACCOUNT_NOT_FOUND = "error.account_not_found";

    public static final String INSUFFICIENT_BALANCE = "error.insufficient_balance";

    public static final String CONCURRENT_MODIFICATION = "error.concurrent_modification";

    public static final String ACCOUNT_INACTIVE = "error.account_inactive";

    public static final String INVALID_AMOUNT = "error.invalid_amount";

    public static final String GENERIC_ERROR = "error.generic";

    public static final String BALANCE_NOT_ZERO = "error.account_not_zero_balance";

    public static final String ACCOUNT_ACCESS_DENIED = "error.account_access_denied";

    public static final String ACCOUNT_TYPE_CANNOT_UPDATE = "error.account_type_cannot_update";

    public static final String UPDATE_BALANCE_FAILED = "error.update_balance_failed";

    public static final String INACTIVE_ACCOUNT_DELETE_FAILURE = "error.inactive_account_delete_failure";

    public static final String ACCOUNT_SELF_MONEY_TRANSFER_FAILURE = "error.account_self_money_transfer_failure";

    public static final String ACCOUNT_NAME_ALREADY_EXIST = "error.account_name_already_exist";

}