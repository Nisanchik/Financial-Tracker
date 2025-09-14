package ru.mirea.newrav1k.userservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.mirea.newrav1k.userservice.mapper.CustomerMapper;
import ru.mirea.newrav1k.userservice.model.dto.CustomerResponse;
import ru.mirea.newrav1k.userservice.model.dto.LoginRequest;
import ru.mirea.newrav1k.userservice.model.dto.RegistrationRequest;
import ru.mirea.newrav1k.userservice.model.entity.Customer;
import ru.mirea.newrav1k.userservice.model.entity.RefreshTokenEntity;
import ru.mirea.newrav1k.userservice.repository.CustomerRepository;
import ru.mirea.newrav1k.userservice.repository.RefreshTokenEntityRepository;
import ru.mirea.newrav1k.userservice.security.token.AccessToken;
import ru.mirea.newrav1k.userservice.security.token.JwtToken;
import ru.mirea.newrav1k.userservice.security.token.RefreshToken;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerService implements UserDetailsService {

    private final RefreshTokenEntityRepository refreshTokenEntityRepository;

    private final CustomerRepository customerRepository;

    private final JwtAuthenticationService jwtAuthenticationService;

    private final CustomerMapper customerMapper;

    public Page<CustomerResponse> findAll(Pageable pageable) {
        log.debug("Finding all customers");
        return this.customerRepository.findAll(pageable)
                .map(this.customerMapper::toCustomerResponse);
    }

    public CustomerResponse findById(UUID customerId) {
        log.debug("Find customer by id: {}", customerId);
        return this.customerRepository.findById(customerId)
                .map(this.customerMapper::toCustomerResponse)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @Transactional
    public JwtToken register(RegistrationRequest request) {
        log.debug("Register new customer");

        validatePassword(request.password(), request.confirmPassword());

        if (this.customerRepository.existsByUsername(request.username())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
        }

        Customer customer = buildCustomerFromRegistrationRequest(request);

        try {
            this.customerRepository.save(customer);
        } catch (DataIntegrityViolationException exception) {
            log.error("User with email already exists (email={})", request.username(), exception);
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Customer with email already exists");
        } catch (DataAccessException exception) {
            log.error("DB error while saving customer: {}", request.username(), exception);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to register user");
        }

        AccessToken accessToken = this.jwtAuthenticationService.generateAccessToken(customer);

        RefreshToken refreshToken = this.jwtAuthenticationService.generateRefreshToken(customer);

        return new JwtToken(accessToken, refreshToken);
    }

    @Transactional
    public JwtToken login(LoginRequest request) {
        log.debug("Login customer");
        Customer customer = this.customerRepository.findByUsername(request.username())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));

        validatePassword(request.password(), customer.getPassword());

        AccessToken accessToken = this.jwtAuthenticationService.generateAccessToken(customer);

        RefreshToken refreshToken = this.jwtAuthenticationService.generateRefreshToken(customer);

        return new JwtToken(accessToken, refreshToken);
    }

    @Transactional
    public JwtToken refresh(String token) {
        log.debug("Refresh customer's token");
        RefreshTokenEntity refreshTokenEntity = this.refreshTokenEntityRepository.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Refresh token not found"));

        if (refreshTokenEntity.getExpiresAt().isBefore(Instant.now())) {
            this.refreshTokenEntityRepository.delete(refreshTokenEntity);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Refresh token expired");
        }

        Customer customer = this.customerRepository.findById(refreshTokenEntity.getCustomerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));

        AccessToken newAccessToken = this.jwtAuthenticationService.generateAccessToken(customer);

        RefreshToken newRefreshToken = this.jwtAuthenticationService.generateRefreshToken(customer);

        return new JwtToken(newAccessToken, newRefreshToken);
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
                .orElseThrow(() -> new UsernameNotFoundException("Customer with " + username + " not found"));
    }

}