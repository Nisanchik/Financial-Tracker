package org.example.transactionservice.configuration;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "org.example.transactionservice.service.client")
public class FeignConfiguration {

}