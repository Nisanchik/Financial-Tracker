package ru.mirea.newrav1k.userservice.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import ru.mirea.newrav1k.userservice.model.dto.AccessToken;
import ru.mirea.newrav1k.userservice.model.dto.RefreshToken;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class JwtAuthenticationService {

    @Setter(onMethod_ = @Autowired)
    @Value("${user-service.jwt.access-token.expiry}")
    private Duration accessTokenExpiration;

    @Setter(onMethod_ = @Autowired)
    @Value("${user-service.jwt.refresh-token.expiry}")
    private Duration refreshTokenExpiration;

    @Value("${user-service.jwt.secret}")
    private String jwtSecret;

    private JwtParser jwtParser;

    @PostConstruct
    public void init() {
        this.jwtParser = Jwts.parser()
                .verifyWith(getSigningKey())
                .build();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(this.jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public AccessToken generateAccessToken(UserDetails userDetails) {
        log.debug("Generating access token for user {}", userDetails.getUsername());
        Instant now = Instant.now();
        String accessToken = Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("authorities", userDetails.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(this.accessTokenExpiration)))
                .signWith(getSigningKey())
                .compact();

        return new AccessToken(accessToken);
    }

    public RefreshToken generateRefreshToken(UserDetails userDetails) {
        log.debug("Generating refresh token for user {}", userDetails.getUsername());
        Instant now = Instant.now();
        String refreshToken = Jwts.builder()
                .subject(userDetails.getUsername())
                .claim("tokenType", "refresh")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(this.refreshTokenExpiration)))
                .signWith(getSigningKey())
                .compact();

        return new RefreshToken(refreshToken);
    }

    public List<GrantedAuthority> getAuthoritiesFromToken(String token) {
        log.debug("Getting authorities for token {}", token);
        Claims claims = this.jwtParser
                .parseSignedClaims(token)
                .getPayload();

        @SuppressWarnings("unchecked")
        List<String> authorities = claims.get("authorities", List.class);

        return authorities.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    public String getSubjectFromToken(String token) {
        log.debug("Getting username for token {}", token);
        Claims claims = this.jwtParser
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }

    public Date getExpirationDateFromToken(String token) {
        Claims claims = this.jwtParser
                .parseSignedClaims(token)
                .getPayload();

        return claims.getExpiration();
    }

    public boolean validateToken(String token) {
        try {
            this.jwtParser
                    .parseSignedClaims(token);
            return true;
        } catch (SecurityException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    public boolean isTokenExpired(String token) {
        try {
            Date expiration = getExpirationDateFromToken(token);
            return expiration.before(new Date());
        } catch (Exception exception) {
            return false;
        }
    }

    public boolean isRefreshToken(String token) {
        try {
            Claims claims = this.jwtParser
                    .parseSignedClaims(token)
                    .getPayload();

            return "refresh".equals(claims.get("tokenType", String.class));
        } catch (Exception exception) {
            return false;
        }
    }

}