package ru.mirea.newrav1k.userservice.controller.rest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.mirea.newrav1k.userservice.model.dto.CustomerResponse;
import ru.mirea.newrav1k.userservice.security.principal.CustomerPrincipal;
import ru.mirea.newrav1k.userservice.service.CustomerService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping("/me")
    public ResponseEntity<CustomerResponse> me(@AuthenticationPrincipal CustomerPrincipal principal) {
        log.info("Request to get information about me");
        CustomerResponse customer = this.customerService.findById(principal.getId());
        return ResponseEntity.ok(customer);
    }

}