package ru.mirea.newrav1k.transactionservice.service.client;

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

    @PostMapping(value = "/api/accounts/{accountId}/update-balance", consumes = "application/json")
    void updateBalance(@PathVariable("accountId") UUID accountId,
                       @RequestParam("transactionId") UUID transactionId,
                       @RequestParam("amount") BigDecimal amount);

    @PostMapping(value = "/api/accounts/{fromAccountId}/transfer/{toAccountId}", consumes = "application/json")
    void transferFunds(@PathVariable("fromAccountId") UUID fromAccountId,
                         @PathVariable("toAccountId") UUID toAccountId,
                         @RequestParam("transactionId") UUID transactionId,
                         @RequestParam("amount") BigDecimal amount);

}