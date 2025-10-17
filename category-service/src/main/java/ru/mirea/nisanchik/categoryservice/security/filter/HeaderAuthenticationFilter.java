package ru.mirea.nisanchik.categoryservice.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.mirea.nisanchik.categoryservice.security.HeaderAuthenticationDetails;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
public class HeaderAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        String tracker = request.getHeader("X-Tracker-Id");
        String trackerAuthorities = request.getHeader("X-Tracker-Authorities");
        if (tracker != null && trackerAuthorities != null) {
            try {
                List<GrantedAuthority> authorities = Arrays.stream(trackerAuthorities.split(","))
                        .map(String::trim)
                        .filter(auth -> !auth.isEmpty())
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());
                HeaderAuthenticationDetails headerAuthenticationToken =
                        new HeaderAuthenticationDetails(UUID.fromString(tracker), authorities);
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(headerAuthenticationToken, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            } catch (Exception exception) {
                log.error("Error while header authenticated", exception);
            }
        }
        filterChain.doFilter(request, response);
    }

}