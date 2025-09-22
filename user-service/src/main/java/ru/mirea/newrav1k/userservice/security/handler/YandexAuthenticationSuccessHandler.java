package ru.mirea.newrav1k.userservice.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.common.contenttype.ContentType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import ru.mirea.newrav1k.userservice.model.entity.YandexToken;
import ru.mirea.newrav1k.userservice.security.principal.YandexOAuth2Tracker;
import ru.mirea.newrav1k.userservice.security.token.JwtToken;
import ru.mirea.newrav1k.userservice.service.JwtAuthenticationService;
import ru.mirea.newrav1k.userservice.service.YandexAuthenticationService;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class YandexAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final OAuth2AuthorizedClientService oAuth2AuthorizedClientService;

    private final YandexAuthenticationService yandexAuthenticationService;

    private final JwtAuthenticationService jwtAuthenticationService;

    private final ObjectMapper objectMapper;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2AuthenticationToken authenticationToken = (OAuth2AuthenticationToken) authentication;
        OAuth2AuthorizedClient oAuth2AuthorizedClient = this.oAuth2AuthorizedClientService.loadAuthorizedClient(
                authenticationToken.getAuthorizedClientRegistrationId(),
                authentication.getName()
        );
        OAuth2AccessToken accessToken = oAuth2AuthorizedClient.getAccessToken();
        OAuth2RefreshToken refreshToken = oAuth2AuthorizedClient.getRefreshToken();

        YandexOAuth2Tracker yandexOAuth2Tracker = (YandexOAuth2Tracker) authentication.getPrincipal();

        YandexToken yandexToken = this.yandexAuthenticationService.buildYandexToken(yandexOAuth2Tracker.getTrackerId(), accessToken, refreshToken);
        JwtToken jwtToken = new JwtToken(
                this.jwtAuthenticationService.generateAccessToken(yandexOAuth2Tracker.getTrackerId(), yandexOAuth2Tracker.getUsername()),
                this.jwtAuthenticationService.generateRefreshToken(yandexOAuth2Tracker.getTrackerId())
        );
        AuthenticationSuccessResponse successResponse = new AuthenticationSuccessResponse(jwtToken, yandexToken);

        response.setContentType(ContentType.APPLICATION_JSON.toString());
        response.getWriter().write(this.objectMapper.writeValueAsString(successResponse));
    }

    private record AuthenticationSuccessResponse(
            JwtToken jwtToken,
            YandexToken yandexToken
    ) {

    }

}