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
import ru.mirea.newrav1k.userservice.model.entity.Tracker;
import ru.mirea.newrav1k.userservice.model.entity.YandexToken;
import ru.mirea.newrav1k.userservice.repository.TrackerRepository;
import ru.mirea.newrav1k.userservice.repository.YandexTokenRepository;
import ru.mirea.newrav1k.userservice.security.core.YandexOAuth2Tracker;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class YandexAuthenticationService extends DefaultOAuth2UserService {

    private final TrackerRepository trackerRepository;

    private final YandexTokenRepository yandexTokenRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String username = attributes.get("default_email").toString();
        String firstname = attributes.get("first_name").toString();
        String lastname = attributes.get("last_name").toString();

        Tracker tracker = this.trackerRepository.findByUsername(username)
                .map(existingTracker -> {
                    existingTracker.setFirstname(firstname);
                    existingTracker.setLastname(lastname);
                    return this.trackerRepository.save(existingTracker);
                })
                .orElseGet(() -> buildTracker(username, firstname, lastname));

        return buildYandexTracker(tracker, attributes);
    }

    @Transactional
    public YandexToken buildYandexToken(UUID trackerId, OAuth2AccessToken accessToken, OAuth2RefreshToken refreshToken) {
        YandexToken yandexToken = new YandexToken();
        yandexToken.setTrackerId(trackerId);
        yandexToken.setAccessToken(accessToken.getTokenValue());
        if (refreshToken != null) {
            yandexToken.setRefreshToken(refreshToken.getTokenValue());
        }
        return this.yandexTokenRepository.save(yandexToken);
    }

    private Tracker buildTracker(String username, String firstname, String lastname) {
        Tracker tracker = new Tracker();
        tracker.setUsername(username);
        tracker.setFirstname(firstname);
        tracker.setLastname(lastname);
        tracker.setPassword(UUID.randomUUID().toString());
        return this.trackerRepository.save(tracker);
    }

    private YandexOAuth2Tracker buildYandexTracker(Tracker tracker, Map<String, Object> attributes) {
        YandexOAuth2Tracker yandexOAuth2Tracker = new YandexOAuth2Tracker();
        yandexOAuth2Tracker.setTrackerId(tracker.getId());
        yandexOAuth2Tracker.setUsername(tracker.getUsername());
        yandexOAuth2Tracker.setFirstname(tracker.getFirstname());
        yandexOAuth2Tracker.setLastname(tracker.getLastname());
        yandexOAuth2Tracker.setAttributes(attributes);
        return yandexOAuth2Tracker;
    }

}