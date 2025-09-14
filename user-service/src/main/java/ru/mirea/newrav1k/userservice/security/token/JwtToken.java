package ru.mirea.newrav1k.userservice.security.token;

public record JwtToken(
        AccessToken accessToken,
        RefreshToken refreshToken
) {

}