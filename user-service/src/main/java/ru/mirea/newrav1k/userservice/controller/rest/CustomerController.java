package ru.mirea.newrav1k.userservice.controller.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

import java.util.UUID;

@Tag(name = "Customer Controller",
        description = "Контроллер для управления клиентами")
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    @Operation(summary = "Получение списка клиентов",
            description = "Получает список клиентов. Доступно только для администратора")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping
    public PagedModel<CustomerResponse> getAllCustomers(@PageableDefault Pageable pageable) {
        log.info("Request to get all customers");
        return new PagedModel<>(this.customerService.findAll(pageable));
    }

    @Operation(summary = "Получение персональной информации",
            description = "Получает персональную информацию клиента по его идентификатору")
    @ApiResponse(responseCode = "404", description = "Клиент не найден")
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public ResponseEntity<CustomerResponse> me(@AuthenticationPrincipal CustomerPrincipal principal) {
        log.info("Request to get information about me");
        CustomerResponse customer = this.customerService.findById(principal.getId());
        return ResponseEntity.ok(customer);
    }

    @Operation(summary = "Изменение персональных данных",
            description = "Изменяет персональные данные клиента по его идентификатору")
    @ApiResponses(value = {@ApiResponse(responseCode = "404", description = "Клиент не найден"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные")})
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/me")
    public ResponseEntity<CustomerResponse> changePersonalInfo(@Valid @RequestBody ChangePersonalInfoRequest request,
                                                               @AuthenticationPrincipal CustomerPrincipal principal) {
        log.info("Request to change personal information");
        CustomerResponse customer = this.customerService.changePersonalInfo(request, principal.getId());
        return ResponseEntity.ok(customer);
    }

    @Operation(summary = "Изменение пароля",
            description = "Изменяет пароль клиента по его идентификатору")
    @ApiResponses(value = {@ApiResponse(responseCode = "404", description = "Клиент не найден"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные")})
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/me/change-password")
    public ResponseEntity<JwtToken> changePassword(@Valid @RequestBody ChangePasswordRequest request,
                                                   @AuthenticationPrincipal CustomerPrincipal principal) {
        log.info("Request to change password");
        JwtToken jwtToken = this.customerService.changePassword(request, principal.getId());
        return ResponseEntity.ok(jwtToken);
    }

    @Operation(summary = "Изменение почты клиента",
            description = "Изменяет почты клиента по его идентификатору")
    @ApiResponses(value = {@ApiResponse(responseCode = "404", description = "Клиент не найден"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные")})
    @PreAuthorize("isAuthenticated()")
    @PutMapping("/me/change-username")
    public ResponseEntity<JwtToken> changeUsername(@Valid @RequestBody ChangeUsernameRequest request,
                                                   @AuthenticationPrincipal CustomerPrincipal principal) {
        log.info("Request to change username");
        JwtToken jwtToken = this.customerService.changeUsername(request, principal.getId());
        return ResponseEntity.ok(jwtToken);
    }

    @Operation(summary = "Удаление своего аккаунта",
            description = "Удаляет аккаунт клиента по его идентификатору")
    @ApiResponse(responseCode = "404", description = "Клиент не найден")
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteSelf(@AuthenticationPrincipal CustomerPrincipal principal) {
        log.info("Request to delete self");
        this.customerService.deleteById(principal.getId());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Удаление своего аккаунта",
            description = "Удаляет аккаунт клиента по его идентификатору. Доступно только для администратора")
    @ApiResponse(responseCode = "404", description = "Клиент не найден")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("/{customerId}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable("customerId") UUID customerId) {
        log.info("Request to delete customer");
        this.customerService.deleteById(customerId);
        return ResponseEntity.noContent().build();
    }

}