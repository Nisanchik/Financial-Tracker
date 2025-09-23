package ru.mirea.newrav1k.userservice.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.mirea.newrav1k.userservice.security.core.TrackerPrincipal;
import ru.mirea.newrav1k.userservice.service.JwtAuthenticationService;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION = "Authorization";

    private static final String BEARER = "Bearer ";

    private final JwtAuthenticationService jwtAuthenticationService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        String authenticationHeader = request.getHeader(AUTHORIZATION);
        if (authenticationHeader != null && authenticationHeader.startsWith(BEARER)) {
            String token = authenticationHeader.substring(7);
            if (this.jwtAuthenticationService.validateToken(token)) {
                try {
                    String subject = this.jwtAuthenticationService.getSubjectFromToken(token);
                    List<GrantedAuthority> authorities = this.jwtAuthenticationService.getAuthoritiesFromToken(token);
                    TrackerPrincipal principal = new TrackerPrincipal(UUID.fromString(subject), authorities);
                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                            new UsernamePasswordAuthenticationToken(principal, null, authorities);
                    usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                } catch (Exception exception) {
                    log.error("Failed to validate token", exception);
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/api/auth") ||
                path.startsWith("/api/public/") ||
                path.startsWith("/swagger-ui/") ||
                path.startsWith("/v3/api-docs/") ||
                path.startsWith("/actuator/health");
    }

}