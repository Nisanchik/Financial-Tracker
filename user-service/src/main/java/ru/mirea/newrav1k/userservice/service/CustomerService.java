package ru.mirea.newrav1k.userservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.mirea.newrav1k.userservice.model.dto.RegistrationRequest;
import ru.mirea.newrav1k.userservice.model.entity.Customer;
import ru.mirea.newrav1k.userservice.repository.CustomerRepository;
import ru.mirea.newrav1k.userservice.security.token.AccessToken;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerService implements UserDetailsService {

    private final CustomerRepository customerRepository;

    private final JwtAuthenticationService jwtAuthenticationService;

    @Transactional
    public AccessToken register(RegistrationRequest request) {
        log.debug("Register new user");

        validatePassword(request.password(), request.confirmPassword());

        if (this.customerRepository.existsByUsername(request.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }

        Customer customer = buildCustomerFromRegistrationRequest(request);

        try {
            this.customerRepository.save(customer);
        } catch (DataIntegrityViolationException exception) {
            log.error("User with email already exists (email={})", request.username(), exception);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User with email already exists");
        }

        return this.jwtAuthenticationService.generateAccessToken(customer);
    }

    private void validatePassword(String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            log.warn("Passwords do not match");
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Passwords do not match");
        }
    }

    private Customer buildCustomerFromRegistrationRequest(RegistrationRequest request) {
        Customer customer = new Customer();
        customer.setUsername(request.username());
        customer.setFirstname(request.firstname());
        customer.setLastname(request.lastname());
        customer.setPassword(request.password());
        return customer;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return this.customerRepository.findByUsername(username)
                .map(user -> User.builder()
                        .username(username)
                        .password("{noop}" + user.getPassword())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("User with " + username + " not found"));
    }

}