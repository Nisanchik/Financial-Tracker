package ru.mirea.newrav1k.userservice.controller.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.mirea.newrav1k.userservice.model.dto.LoginRequest;
import ru.mirea.newrav1k.userservice.model.dto.RegistrationRequest;
import ru.mirea.newrav1k.userservice.security.token.JwtToken;
import ru.mirea.newrav1k.userservice.service.CustomerService;
import ru.mirea.newrav1k.userservice.service.JwtAuthenticationService;

import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "Authentication Controller",
        description = "Контроллер для управления аутентификацией в приложении")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final JwtAuthenticationService jwtAuthenticationService;

    private final CustomerService customerService;

    @Operation(summary = "Регистрация клиента",
            description = "Регистрирует клиента и выдаёт ему jwt токен")
    @ApiResponses(value = {@ApiResponse(responseCode = "400", description = "Некорректные данные"),
            @ApiResponse(responseCode = "409", description = "Невозможно зарегистрировать клиента")})
    @PreAuthorize("isAnonymous()")
    @PostMapping("/register")
    public ResponseEntity<JwtToken> register(@Valid @RequestBody RegistrationRequest request) {
        log.info("Request to register: {}", request);
        JwtToken token = this.customerService.register(request);
        return ResponseEntity.ok(token);
    }

    @Operation(summary = "Авторизация клиента",
            description = "Проводит авторизацию клиента в системе")
    @ApiResponse(responseCode = "400", description = "Некорректные данные")
    @PreAuthorize("isAnonymous()")
    @PostMapping("/login")
    public ResponseEntity<JwtToken> login(@Valid @RequestBody LoginRequest request) {
        log.info("Request to login: {}", request);
        JwtToken token = this.customerService.login(request);
        return ResponseEntity.ok(token);
    }

    @Operation(summary = "Обновление jwt токена",
            description = "Обновляет jwt токен и выдаёт новый")
    @ApiResponses(value = {@ApiResponse(responseCode = "409", description = "Невалидный токен"),
            @ApiResponse(responseCode = "404", description = "Клиент не найден")})
    @PreAuthorize("isAnonymous()")
    @PostMapping("/refresh")
    public ResponseEntity<JwtToken> refresh(@RequestParam("token") String token) {
        log.info("Request to refresh token: {}", token);
        JwtToken refreshedToken = this.customerService.refresh(token);
        return ResponseEntity.ok(refreshedToken);
    }

    @Operation(summary = "Удаление refresh-токена",
            description = "Удаляет refresh-токен клиента")
    @ApiResponse(responseCode = "409", description = "Невалидный токен")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestParam("token") String token,
                                       @RequestParam(value = "logoutAll", required = false, defaultValue = "false") boolean isLogoutAll) {
        log.info("Request to logout token: {}", token);
        this.customerService.logout(token, isLogoutAll);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("isAnonymous()")
    @GetMapping("/.well-known/jwks.json")
    public ResponseEntity<Map<String, Object>> getPublicJwts() {
        log.info("Request to retrieve public jwks");
        RSAPublicKey rsaPublicKey = (RSAPublicKey) this.jwtAuthenticationService.getPublicKey();
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
        return ResponseEntity.ok(Map.of("keys", List.of(jwk)));
    }

}