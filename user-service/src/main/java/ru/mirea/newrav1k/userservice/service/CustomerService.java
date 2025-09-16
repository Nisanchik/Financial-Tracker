package ru.mirea.newrav1k.userservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.mirea.newrav1k.userservice.exception.CustomerAlreadyExistsException;
import ru.mirea.newrav1k.userservice.exception.CustomerNotFoundException;
import ru.mirea.newrav1k.userservice.exception.JwtExpiredException;
import ru.mirea.newrav1k.userservice.exception.PasswordMismatchException;
import ru.mirea.newrav1k.userservice.exception.UserServiceException;
import ru.mirea.newrav1k.userservice.mapper.CustomerMapper;
import ru.mirea.newrav1k.userservice.model.dto.ChangePasswordRequest;
import ru.mirea.newrav1k.userservice.model.dto.ChangePersonalInfoRequest;
import ru.mirea.newrav1k.userservice.model.dto.ChangeUsernameRequest;
import ru.mirea.newrav1k.userservice.model.dto.CustomerResponse;
import ru.mirea.newrav1k.userservice.model.dto.LoginRequest;
import ru.mirea.newrav1k.userservice.model.dto.RegistrationRequest;
import ru.mirea.newrav1k.userservice.model.entity.Customer;
import ru.mirea.newrav1k.userservice.repository.CustomerRepository;
import ru.mirea.newrav1k.userservice.security.token.AccessToken;
import ru.mirea.newrav1k.userservice.security.token.JwtToken;
import ru.mirea.newrav1k.userservice.security.token.RefreshToken;

import java.util.UUID;

import static ru.mirea.newrav1k.userservice.utils.MessageCode.REGISTRATION_FAILED;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerService implements UserDetailsService {

    private final CustomerRepository customerRepository;

    private final JwtAuthenticationService jwtAuthenticationService;

    private final CustomerMapper customerMapper;

    public final PasswordEncoder passwordEncoder;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Page<CustomerResponse> findAll(Pageable pageable) {
        log.debug("Finding all customers");
        return this.customerRepository.findAll(pageable)
                .map(this.customerMapper::toCustomerResponse);
    }

    @PreAuthorize("@securityUtils.isSelfOrAdmin(#customerId, authentication)")
    public CustomerResponse findById(UUID customerId) {
        log.debug("Find customer by id: {}", customerId);
        return this.customerRepository.findById(customerId)
                .map(this.customerMapper::toCustomerResponse)
                .orElseThrow(CustomerNotFoundException::new);
    }

    @PreAuthorize("isAnonymous()")
    @Transactional
    public JwtToken register(RegistrationRequest request) {
        log.debug("Register new customer");
        validatePasswordMatch(request.password(), request.confirmPassword());

        if (this.customerRepository.existsByUsername(request.username())) {
            throw new CustomerAlreadyExistsException();
        }

        Customer customer = buildCustomerFromRegistrationRequest(request);

        try {
            this.customerRepository.save(customer);
        } catch (DataIntegrityViolationException exception) {
            log.error("User with email already exists (email={})", request.username(), exception);
            throw new CustomerAlreadyExistsException();
        } catch (DataAccessException exception) {
            log.error("Database error while saving customer: {}", request.username(), exception);
            throw new UserServiceException(REGISTRATION_FAILED, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return generateJwtToken(customer);
    }

    @PreAuthorize("isAnonymous()")
    @Transactional
    public JwtToken login(LoginRequest request) {
        log.debug("Login customer");
        Customer customer = this.customerRepository.findByUsername(request.username())
                .orElseThrow(CustomerNotFoundException::new);

        validatePassword(request.password(), customer.getPassword());

        this.jwtAuthenticationService.invalidateRefreshTokens(customer.getId());

        return generateJwtToken(customer);
    }

    @PreAuthorize("isAnonymous()")
    @Transactional
    public JwtToken refresh(String token) {
        log.debug("Refresh customer's token");
        String subject = this.jwtAuthenticationService.getSubjectFromToken(token);

        if (this.jwtAuthenticationService.isTokenExpired(token)) {
            this.jwtAuthenticationService.invalidateRefreshToken(token);
            throw new JwtExpiredException();
        }

        Customer customer = this.customerRepository.findById(UUID.fromString(subject))
                .orElseThrow(CustomerNotFoundException::new);

        this.jwtAuthenticationService.invalidateRefreshTokens(customer.getId());

        return generateJwtToken(customer);
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional
    public void logout(String token, boolean isLogoutAll) {
        log.debug("Logout customer's token");
        String subject = this.jwtAuthenticationService.getSubjectFromToken(token);
        if (isLogoutAll) {
            this.jwtAuthenticationService.invalidateRefreshTokens(UUID.fromString(subject));
        } else {
            this.jwtAuthenticationService.invalidateRefreshToken(token);
        }
    }

    @PreAuthorize("@securityUtils.isSelfOrAdmin(#customerId, authentication)")
    @Transactional
    public CustomerResponse changePersonalInfo(ChangePersonalInfoRequest request, UUID customerId) {
        log.debug("Change personal information");
        Customer customer = this.customerRepository.findById(customerId)
                .orElseThrow(CustomerNotFoundException::new);
        customer.setFirstname(request.firstname());
        customer.setLastname(request.lastname());
        return this.customerMapper.toCustomerResponse(customer);
    }

    @PreAuthorize("@securityUtils.isSelfOrAdmin(#customerId, authentication)")
    @Transactional
    public JwtToken changePassword(ChangePasswordRequest request, UUID customerId) {
        log.debug("Change password customer");
        validatePasswordMatch(request.password(), request.confirmPassword());

        Customer customer = this.customerRepository.findById(customerId)
                .orElseThrow(CustomerNotFoundException::new);
        customer.setPassword(this.passwordEncoder.encode(request.password()));

        this.jwtAuthenticationService.invalidateRefreshTokens(customer.getId());

        return generateJwtToken(customer);
    }

    @PreAuthorize("@securityUtils.isSelfOrAdmin(#customerId, authentication)")
    @Transactional
    public JwtToken changeUsername(ChangeUsernameRequest request, UUID customerId) {
        log.debug("Change username customer");
        Customer customer = this.customerRepository.findById(customerId)
                .orElseThrow(CustomerNotFoundException::new);
        validatePassword(request.confirmPassword(), customer.getPassword());

        if (this.customerRepository.existsByUsername(request.username())) {
            throw new CustomerAlreadyExistsException();
        }
        customer.setUsername(request.username());

        this.jwtAuthenticationService.invalidateRefreshTokens(customer.getId());

        return generateJwtToken(customer);
    }

    @PreAuthorize("@securityUtils.isSelfOrAdmin(#customerId, authentication)")
    @Transactional
    public void deleteById(UUID customerId) {
        log.debug("Delete customer");
        this.jwtAuthenticationService.invalidateRefreshTokens(customerId);
        this.customerRepository.deleteById(customerId);
    }

    private void validatePasswordMatch(String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            throw new PasswordMismatchException();
        }
    }

    private void validatePassword(String rawPassword, String encodedPassword) {
        if (!this.passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new PasswordMismatchException();
        }
    }

    private Customer buildCustomerFromRegistrationRequest(RegistrationRequest request) {
        Customer customer = new Customer();
        customer.setUsername(request.username());
        customer.setFirstname(request.firstname());
        customer.setLastname(request.lastname());
        customer.setPassword(this.passwordEncoder.encode(request.password()));
        return customer;
    }

    private JwtToken generateJwtToken(Customer customer) {
        AccessToken newAccessToken = this.jwtAuthenticationService.generateAccessToken(customer);
        RefreshToken newRefreshToken = this.jwtAuthenticationService.generateRefreshToken(customer);
        return new JwtToken(newAccessToken, newRefreshToken);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return this.customerRepository.findByUsername(username)
                .map(user -> User.builder()
                        .username(username)
                        .password(this.passwordEncoder.encode(user.getPassword()))
                        .roles("USER")
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("Customer with " + username + " not found"));
    }

}