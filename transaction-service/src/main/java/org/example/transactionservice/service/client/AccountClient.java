package org.example.transactionservice.service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@FeignClient(
        name = "account-service",
        url = "${transaction-service.services.account-service.base-url}"
)
public interface AccountClient {

    @PatchMapping("/api/accounts/{accountId}/withdraw-balance")
    void withdrawBalance(@PathVariable("accountId") UUID accountId, BigDecimal amount);

    @PatchMapping("/api/accounts/{accountId}/deposit-balance")
    void depositBalance(@PathVariable("accountId") UUID accountId, BigDecimal amount);

}