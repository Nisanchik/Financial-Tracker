package ru.mirea.newrav1k.userservice.security.token;

import java.time.Instant;

public record AccessToken(
        String accessToken,
        Instant expiresAt
) {

}