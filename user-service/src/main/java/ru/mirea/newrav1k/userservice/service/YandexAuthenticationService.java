package ru.mirea.newrav1k.userservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mirea.newrav1k.userservice.model.entity.Customer;
import ru.mirea.newrav1k.userservice.model.entity.YandexToken;
import ru.mirea.newrav1k.userservice.repository.CustomerRepository;
import ru.mirea.newrav1k.userservice.repository.YandexTokenRepository;
import ru.mirea.newrav1k.userservice.security.principal.YandexCustomer;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class YandexAuthenticationService extends DefaultOAuth2UserService {

    private final CustomerRepository customerRepository;

    private final YandexTokenRepository yandexTokenRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String username = attributes.get("default_email").toString();
        String firstname = attributes.get("first_name").toString();
        String lastname = attributes.get("last_name").toString();

        Customer customer = this.customerRepository.findByUsername(username)
                .map(existingCustomer -> {
                    existingCustomer.setFirstname(firstname);
                    existingCustomer.setLastname(lastname);
                    return this.customerRepository.save(existingCustomer);
                })
                .orElseGet(() -> buildCustomer(username, firstname, lastname));

        return buildYandexCustomer(customer, attributes);
    }

    @Transactional
    public YandexToken buildYandexToken(UUID userId, OAuth2AccessToken accessToken, OAuth2RefreshToken refreshToken) {
        YandexToken yandexToken = new YandexToken();
        yandexToken.setCustomerId(userId);
        yandexToken.setAccessToken(accessToken.getTokenValue());
        if (refreshToken != null) {
            yandexToken.setRefreshToken(refreshToken.getTokenValue());
        }
        return this.yandexTokenRepository.save(yandexToken);
    }

    private Customer buildCustomer(String username, String firstname, String lastname) {
        Customer customer = new Customer();
        customer.setUsername(username);
        customer.setFirstname(firstname);
        customer.setLastname(lastname);
        customer.setPassword(UUID.randomUUID().toString());
        return this.customerRepository.save(customer);
    }

    private YandexCustomer buildYandexCustomer(Customer customer, Map<String, Object> attributes) {
        YandexCustomer yandexCustomer = new YandexCustomer();
        yandexCustomer.setCustomerId(customer.getId());
        yandexCustomer.setUsername(customer.getUsername());
        yandexCustomer.setFirstname(customer.getFirstname());
        yandexCustomer.setLastname(customer.getLastname());
        yandexCustomer.setAttributes(attributes);
        return yandexCustomer;
    }

}