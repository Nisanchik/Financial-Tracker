package ru.mirea.newrav1k.userservice.controller.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.mirea.newrav1k.userservice.model.dto.LoginRequest;
import ru.mirea.newrav1k.userservice.model.dto.RegistrationRequest;
import ru.mirea.newrav1k.userservice.security.token.JwtToken;
import ru.mirea.newrav1k.userservice.service.CustomerService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final CustomerService customerService;

    @PostMapping("/register")
    public ResponseEntity<JwtToken> register(@Valid @RequestBody RegistrationRequest request) {
        log.info("Request to register: {}", request);
        JwtToken token = this.customerService.register(request);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/login")
    public ResponseEntity<JwtToken> login(@Valid @RequestBody LoginRequest request) {
        log.info("Request to login: {}", request);
        JwtToken token = this.customerService.login(request);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtToken> refresh(@RequestParam("token") String token) {
        log.info("Request to refresh token: {}", token);
        JwtToken refreshedToken = this.customerService.refresh(token);
        return ResponseEntity.ok(refreshedToken);
    }

}