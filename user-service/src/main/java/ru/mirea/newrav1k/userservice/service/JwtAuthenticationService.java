package ru.mirea.newrav1k.userservice.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mirea.newrav1k.userservice.exception.RSAKeyLoadException;
import ru.mirea.newrav1k.userservice.model.entity.Customer;
import ru.mirea.newrav1k.userservice.model.entity.RefreshTokenEntity;
import ru.mirea.newrav1k.userservice.model.enums.Authority;
import ru.mirea.newrav1k.userservice.repository.RefreshTokenEntityRepository;
import ru.mirea.newrav1k.userservice.security.token.AccessToken;
import ru.mirea.newrav1k.userservice.security.token.RefreshToken;
import ru.mirea.newrav1k.userservice.utils.KeyUtils;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtAuthenticationService {

    private final RefreshTokenEntityRepository refreshTokenEntityRepository;

    @Value("${user-service.jwt.access-token.expiry}")
    private Duration accessTokenExpiration;

    @Value("${user-service.jwt.refresh-token.expiry}")
    private Duration refreshTokenExpiration;

    @Value("${user-service.jwt.private-key-path:keys/private_pkcs8.pem}")
    private String privateKeyPath;

    @Value("${user-service.jwt.public-key-path:keys/public.pem}")
    private String publicKeyPath;

    private JwtParser jwtParser;

    private PrivateKey privateKey;

    @Getter
    private PublicKey publicKey;

    @PostConstruct
    public void init() {
        try {
            this.privateKey = KeyUtils.loadPrivateKey(this.privateKeyPath);
            this.publicKey = KeyUtils.loadPublicKey(this.publicKeyPath);

            this.jwtParser = Jwts.parser()
                    .verifyWith(this.publicKey)
                    .clockSkewSeconds(30L)
                    .build();

            log.info("RSA keys loaded successfully");
        } catch (Exception exception) {
            log.error("RSA keys could not be loaded", exception);
            throw new RSAKeyLoadException("Failed to load RSA keys", exception);
        }
    }

    public AccessToken generateAccessToken(Customer customer) {
        log.debug("Generating access token for user {}", customer.getId());
        Instant now = Instant.now();
        String accessToken = Jwts.builder()
                .subject(customer.getId().toString())
                .claim("username", customer.getUsername())
                .claim("authorities", customer.getAuthorities().stream()
                        .map(Authority::name)
                        .map(SimpleGrantedAuthority::new)
                        .map(SimpleGrantedAuthority::getAuthority)
                        .toList())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(this.accessTokenExpiration)))
                .signWith(this.privateKey)
                .compact();

        return new AccessToken(accessToken, now.plus(this.accessTokenExpiration));
    }

    public AccessToken generateAccessToken(UUID userId, String username) {
        log.debug("Generating access token for user {} with username {}", userId, username);
        Instant now = Instant.now();
        String accessToken = Jwts.builder()
                .subject(userId.toString())
                .claim("username", username)
                .claim("authorities", "ROLE_USER")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(this.accessTokenExpiration)))
                .signWith(this.privateKey)
                .compact();

        return new AccessToken(accessToken, now.plus(this.accessTokenExpiration));
    }

    @Transactional
    public RefreshToken generateRefreshToken(UUID userId) {
        log.debug("Generating refresh token for user {}", userId);
        Instant now = Instant.now();

        String uuidToken = UUID.randomUUID().toString();

        RefreshTokenEntity refreshTokenEntity = new RefreshTokenEntity();
        refreshTokenEntity.setCustomerId(userId);
        refreshTokenEntity.setToken(uuidToken);
        refreshTokenEntity.setCreatedAt(now);
        refreshTokenEntity.setExpiresAt(now.plus(this.refreshTokenExpiration));

        this.refreshTokenEntityRepository.save(refreshTokenEntity);

        return new RefreshToken(uuidToken, refreshTokenEntity.getExpiresAt());
    }

    public Map<String, Object> generateJwks() {
        RSAPublicKey rsaPublicKey = (RSAPublicKey) this.getPublicKey();
        Map<String, Object> jwk = new LinkedHashMap<>(Map.of(
                "kty", "RSA",
                "kid", "rsa-key-1",
                "alg", "RS256",
                "use", "sig",
                "n", Base64.getUrlEncoder().withoutPadding()
                        .encodeToString(rsaPublicKey.getModulus().toByteArray()),
                "e", Base64.getUrlEncoder().withoutPadding()
                        .encodeToString(rsaPublicKey.getPublicExponent().toByteArray())
        ));
        // TODO: посмотреть реализацию динамического kid'a и ротации ключей
        return Map.of("keys", List.of(jwk));
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
                .map(GrantedAuthority.class::cast)
                .toList();
    }

    public String getSubjectFromToken(String token) {
        log.debug("Getting username for token {}", token);
        try {
            Claims claims = this.jwtParser
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getSubject();
        } catch (ExpiredJwtException exception) {
            return exception.getClaims().getSubject();
        }
    }

    @Transactional
    public void invalidateRefreshToken(String token) {
        this.refreshTokenEntityRepository.deleteByToken(token);
    }

    @Transactional
    public void invalidateRefreshTokens(UUID customerId) {
        this.refreshTokenEntityRepository.deleteAllByCustomerId(customerId);
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

}