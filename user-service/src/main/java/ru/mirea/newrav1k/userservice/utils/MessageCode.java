package ru.mirea.newrav1k.userservice.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MessageCode {

    public static final String PASSWORD_MISMATCH = "error.password_mismatch";

    public static final String TRACKER_NOT_FOUND = "error.tracker_not_found";

    public static final String TRACKER_ALREADY_EXISTS = "error.tracker_already_exists";

    public static final String REGISTRATION_FAILED = "error.registration_failed";

    public static final String JWT_EXPIRED = "error.jwt_expired";

    public static final String REFRESH_TOKEN_NOT_FOUND = "error.refresh_token_not_found";

}