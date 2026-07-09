package it.interno.platform.starter.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JwtTokenProvider {

    /**
     * Retrieves the JWT token of the currently authenticated request from the
     * SecurityContextHolder.
     *
     * @return the token string, or null if not authenticated or no token found.
     */
    public String getToken() {
        log.info("JwtTokenProvider -> getToken() called");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getCredentials() instanceof String credentials) {
            return credentials;
        }
        return null;
    }
}
