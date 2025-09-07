package ru.mirea.newrav1k.transactionservice.configuration;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "ru.mirea.newrav1k.transactionservice.service.client")
public class FeignConfiguration {

}