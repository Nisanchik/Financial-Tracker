package ru.mirea.newrav1k.userservice.security.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import ru.mirea.newrav1k.userservice.security.principal.TrackerPrincipal;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class TrackerJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt source) {
        UUID subject = UUID.fromString(source.getSubject());
        List<String> authorities =
                Objects.requireNonNullElse(source.getClaimAsStringList("authorities"), Collections.emptyList());

        TrackerPrincipal principal = new TrackerPrincipal(subject, authorities.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList()));

        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }

}