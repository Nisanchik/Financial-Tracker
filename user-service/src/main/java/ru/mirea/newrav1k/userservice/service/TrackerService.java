package ru.mirea.newrav1k.userservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
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
import ru.mirea.newrav1k.userservice.exception.TrackerAlreadyExistsException;
import ru.mirea.newrav1k.userservice.exception.TrackerNotFoundException;
import ru.mirea.newrav1k.userservice.exception.JwtExpiredException;
import ru.mirea.newrav1k.userservice.exception.PasswordMismatchException;
import ru.mirea.newrav1k.userservice.exception.RefreshTokenNotFoundException;
import ru.mirea.newrav1k.userservice.exception.UserServiceException;
import ru.mirea.newrav1k.userservice.mapper.TrackerMapper;
import ru.mirea.newrav1k.userservice.model.dto.ChangePasswordRequest;
import ru.mirea.newrav1k.userservice.model.dto.ChangePersonalInfoRequest;
import ru.mirea.newrav1k.userservice.model.dto.ChangeUsernameRequest;
import ru.mirea.newrav1k.userservice.model.dto.LoginRequest;
import ru.mirea.newrav1k.userservice.model.dto.RegistrationRequest;
import ru.mirea.newrav1k.userservice.model.dto.TrackerResponse;
import ru.mirea.newrav1k.userservice.model.entity.Tracker;
import ru.mirea.newrav1k.userservice.model.enums.Authority;
import ru.mirea.newrav1k.userservice.repository.RefreshTokenRepository;
import ru.mirea.newrav1k.userservice.repository.TrackerRepository;
import ru.mirea.newrav1k.userservice.security.token.AccessToken;
import ru.mirea.newrav1k.userservice.security.token.JwtToken;
import ru.mirea.newrav1k.userservice.security.token.RefreshToken;

import java.time.Instant;
import java.util.UUID;

import static ru.mirea.newrav1k.userservice.utils.MessageCode.REGISTRATION_FAILED;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TrackerService implements UserDetailsService {

    private final RefreshTokenRepository refreshTokenRepository;

    private final TrackerRepository trackerRepository;

    private final JwtAuthenticationService jwtAuthenticationService;

    private final TrackerMapper trackerMapper;

    private final PasswordEncoder passwordEncoder;

    @Cacheable(value = "profile-pages", key = "#pageable.pageNumber + '-' + #pageable.pageSize")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public Page<TrackerResponse> findAll(Pageable pageable) {
        log.debug("Finding all trackers");
        return this.trackerRepository.findAll(pageable)
                .map(this.trackerMapper::toTrackerResponse);
    }

    @Cacheable(value = "profile-details", key = "#trackerId")
    @PreAuthorize("@securityUtils.isSelfOrAdmin(#trackerId, authentication)")
    public TrackerResponse findById(UUID trackerId) {
        log.debug("Find tracker by id: {}", trackerId);
        return this.trackerRepository.findById(trackerId)
                .map(this.trackerMapper::toTrackerResponse)
                .orElseThrow(TrackerNotFoundException::new);
    }

    @CacheEvict(value = "profile-pages", allEntries = true)
    @PreAuthorize("isAnonymous()")
    @Transactional
    public JwtToken register(RegistrationRequest request) {
        log.debug("Register new tracker");
        validatePasswordMatch(request.password(), request.confirmPassword());

        if (this.trackerRepository.existsByUsername(request.username())) {
            throw new TrackerAlreadyExistsException();
        }

        Tracker tracker = buildtrackerFromRegistrationRequest(request);

        try {
            this.trackerRepository.save(tracker);
        } catch (DataIntegrityViolationException exception) {
            log.error("Tracker with email already exists (email={})", request.username(), exception);
            throw new TrackerAlreadyExistsException();
        } catch (DataAccessException exception) {
            log.error("Database error while saving tracker: {}", request.username(), exception);
            throw new UserServiceException(REGISTRATION_FAILED, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return generateJwtToken(tracker);
    }

    @PreAuthorize("isAnonymous()")
    @Transactional
    public JwtToken login(LoginRequest request) {
        log.debug("Login tracker");
        Tracker tracker = this.trackerRepository.findByUsername(request.username())
                .orElseThrow(TrackerNotFoundException::new);

        validatePassword(request.password(), tracker.getPassword());

        this.jwtAuthenticationService.invalidateRefreshTokens(tracker.getId());

        return generateJwtToken(tracker);
    }

    @PreAuthorize("isAnonymous()")
    @Transactional
    public JwtToken refresh(String token) {
        log.debug("Refresh tracker's token");

        ru.mirea.newrav1k.userservice.model.entity.RefreshToken refreshToken =
                this.refreshTokenRepository.findByToken(token)
                        .orElseThrow(RefreshTokenNotFoundException::new);

        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            this.refreshTokenRepository.delete(refreshToken);
            throw new JwtExpiredException();
        }

        Tracker tracker = this.trackerRepository.findById(refreshToken.getTrackerId())
                .orElseThrow(TrackerNotFoundException::new);

        this.jwtAuthenticationService.invalidateRefreshTokens(tracker.getId());

        return generateJwtToken(tracker);
    }

    @PreAuthorize("isAuthenticated()")
    @Transactional
    public void logout(String token, boolean isLogoutAll) {
        log.debug("Logout tracker's token");
        ru.mirea.newrav1k.userservice.model.entity.RefreshToken refreshToken =
                this.refreshTokenRepository.findByToken(token)
                        .orElseThrow(RefreshTokenNotFoundException::new);
        if (isLogoutAll) {
            this.jwtAuthenticationService.invalidateRefreshTokens(refreshToken.getTrackerId());
        } else {
            this.jwtAuthenticationService.invalidateRefreshToken(token);
        }
    }

    @Caching(evict = {
            @CacheEvict(value = "profile-details", key = "#trackerId"),
            @CacheEvict(value = "profile-pages", allEntries = true)
    })
    @PreAuthorize("@securityUtils.isSelfOrAdmin(#trackerId, authentication)")
    @Transactional
    public TrackerResponse changePersonalInfo(ChangePersonalInfoRequest request, UUID trackerId) {
        log.debug("Change personal information");
        Tracker tracker = this.trackerRepository.findById(trackerId)
                .orElseThrow(TrackerNotFoundException::new);
        tracker.setFirstname(request.firstname());
        tracker.setLastname(request.lastname());
        return this.trackerMapper.toTrackerResponse(tracker);
    }

    @PreAuthorize("@securityUtils.isSelfOrAdmin(#trackerId, authentication)")
    @Transactional
    public JwtToken changePassword(ChangePasswordRequest request, UUID trackerId) {
        log.debug("Change password tracker");
        validatePasswordMatch(request.password(), request.confirmPassword());

        Tracker tracker = this.trackerRepository.findById(trackerId)
                .orElseThrow(TrackerNotFoundException::new);
        tracker.setPassword(this.passwordEncoder.encode(request.password()));

        this.jwtAuthenticationService.invalidateRefreshTokens(tracker.getId());

        return generateJwtToken(tracker);
    }

    @Caching(evict = {
            @CacheEvict(value = "profile-details", key = "#trackerId"),
            @CacheEvict(value = "profile-pages", allEntries = true)
    })
    @PreAuthorize("@securityUtils.isSelfOrAdmin(#trackerId, authentication)")
    @Transactional
    public JwtToken changeUsername(ChangeUsernameRequest request, UUID trackerId) {
        log.debug("Change username tracker");
        Tracker tracker = this.trackerRepository.findById(trackerId)
                .orElseThrow(TrackerNotFoundException::new);
        validatePassword(request.confirmPassword(), tracker.getPassword());

        if (this.trackerRepository.existsByUsername(request.username())) {
            throw new TrackerAlreadyExistsException();
        }
        tracker.setUsername(request.username());

        this.jwtAuthenticationService.invalidateRefreshTokens(tracker.getId());

        return generateJwtToken(tracker);
    }

    @Caching(evict = {
            @CacheEvict(value = "profile-details", key = "#trackerId"),
            @CacheEvict(value = "profile-pages", allEntries = true)
    })
    @PreAuthorize("@securityUtils.isSelfOrAdmin(#trackerId, authentication)")
    @Transactional
    public void deleteById(UUID trackerId) {
        log.debug("Delete tracker");
        this.jwtAuthenticationService.invalidateRefreshTokens(trackerId);
        this.trackerRepository.deleteById(trackerId);
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

    private Tracker buildtrackerFromRegistrationRequest(RegistrationRequest request) {
        Tracker tracker = new Tracker();
        tracker.setUsername(request.username());
        tracker.setFirstname(request.firstname());
        tracker.setLastname(request.lastname());
        tracker.setPassword(this.passwordEncoder.encode(request.password()));
        return tracker;
    }

    private JwtToken generateJwtToken(Tracker tracker) {
        AccessToken newAccessToken = this.jwtAuthenticationService.generateAccessToken(tracker);
        RefreshToken newRefreshToken = this.jwtAuthenticationService.generateRefreshToken(tracker.getId());
        return new JwtToken(newAccessToken, newRefreshToken);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return this.trackerRepository.findByUsername(username)
                .map(user -> User.builder()
                        .username(username)
                        .password(user.getPassword())
                        .roles(user.getAuthorities().stream()
                                .map(Authority::name)
                                .toArray(String[]::new))
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("Tracker with " + username + " not found"));
    }

}