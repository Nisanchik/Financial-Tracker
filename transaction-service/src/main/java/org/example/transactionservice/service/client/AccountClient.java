package org.example.transactionservice.service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@FeignClient(
        name = "account-service",
        url = "${transaction-service.services.account-service.base-url}"
)
public interface AccountClient {

    @Deprecated(forRemoval = true)
    @PostMapping(value = "/api/accounts/{accountId}/withdraw-balance", consumes = "application/json")
    void withdrawBalance(@PathVariable("accountId") UUID accountId, @RequestParam("amount") BigDecimal amount);

    @Deprecated(forRemoval = true)
    @PostMapping(value = "/api/accounts/{accountId}/deposit-balance", consumes = "application/json")
    void depositBalance(@PathVariable("accountId") UUID accountId, @RequestParam("amount") BigDecimal amount);

    @PostMapping(value = "/api/accounts/{accountId}/update-balance", consumes = "application/json")
    void updateBalance(@PathVariable("accountId") UUID accountId, @RequestParam("amount") BigDecimal amount);

}