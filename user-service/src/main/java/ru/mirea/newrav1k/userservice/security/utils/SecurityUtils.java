package ru.mirea.newrav1k.userservice.security.utils;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import ru.mirea.newrav1k.userservice.security.core.TrackerPrincipal;

import java.util.UUID;

@Component
public class SecurityUtils {

    public boolean isSelfOrAdmin(UUID trackerId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        TrackerPrincipal principal = (TrackerPrincipal) authentication.getPrincipal();
        return principal.getTrackerId().equals(trackerId) || isAdmin;
    }

}