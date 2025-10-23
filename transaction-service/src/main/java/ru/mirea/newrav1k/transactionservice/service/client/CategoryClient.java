package ru.mirea.newrav1k.transactionservice.service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(
        value = "category-service",
        url = "${transaction-service.services.category-service.base-url}"
)
public interface CategoryClient {

    @GetMapping("/api/categories/{categoryId}/exists")
    boolean existsCategoryById(@PathVariable("categoryId") UUID categoryId);

}