package ru.mirea.newrav1k.userservice.controller.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.mirea.newrav1k.userservice.model.dto.ChangePasswordRequest;
import ru.mirea.newrav1k.userservice.model.dto.ChangePersonalInfoRequest;
import ru.mirea.newrav1k.userservice.model.dto.ChangeUsernameRequest;
import ru.mirea.newrav1k.userservice.model.dto.CustomerResponse;
import ru.mirea.newrav1k.userservice.security.principal.CustomerPrincipal;
import ru.mirea.newrav1k.userservice.security.token.JwtToken;
import ru.mirea.newrav1k.userservice.service.CustomerService;

@Tag(name = "Customer Controller",
        description = "Контроллер для управления клиентами")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    @Operation(summary = "Получение персональной информации",
            description = "Получает персональную информацию клиента по его идентификатору")
    @ApiResponse(responseCode = "404", description = "Клиент не найден")
    @GetMapping("/me")
    public ResponseEntity<CustomerResponse> me(@AuthenticationPrincipal CustomerPrincipal principal) {
        log.info("Request to get information about me");
        CustomerResponse customer = this.customerService.findById(principal.getId());
        return ResponseEntity.ok(customer);
    }

}