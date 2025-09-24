package ru.mirea.newrav1k.transactionservice.configuration;

import feign.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.mirea.newrav1k.transactionservice.security.HeaderAuthenticationDetails;

import java.util.stream.Collectors;

@Slf4j
@Configuration
@EnableFeignClients(basePackages = "ru.mirea.newrav1k.transactionservice.service.client")
public class FeignConfiguration {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            SecurityContext securityContext = SecurityContextHolder.getContext();
            Authentication authentication = securityContext.getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                HeaderAuthenticationDetails authenticationDetails = (HeaderAuthenticationDetails) authentication.getPrincipal();
                requestTemplate.header("X-Tracker-Id", authenticationDetails.getTrackerId().toString());
                requestTemplate.header("X-Tracker-Authorities", authentication.getAuthorities().stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.joining(", ")));
            }
        };
    }

}