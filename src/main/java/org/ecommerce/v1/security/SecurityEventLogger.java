package org.ecommerce.v1.security;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;


@Component
@Slf4j
public class SecurityEventLogger {

    @EventListener
    public void onAuthenticationSuccess(AuthenticationSuccessEvent event) {
        Authentication auth = event.getAuthentication();
        String username = auth != null ? auth.getName() : "unknown";
        String remote = currentRemoteAddress();
        log.info("SECURITY_AUTH_SUCCESS | username={} | remote={} | principal={}",
                username, remote, auth != null ? auth.getClass().getSimpleName() : "null");
    }

    @EventListener
    public void onAuthenticationFailure(AbstractAuthenticationFailureEvent event) {
        Authentication auth = event.getAuthentication();
        String username = auth != null ? auth.getName() : "unknown";
        String remote = currentRemoteAddress();
        String cause = event.getException() != null ? event.getException().getMessage() : "unknown";
        log.warn("SECURITY_AUTH_FAILURE | username={} | remote={} | cause={}", username, remote, cause);
    }

    private static String currentRemoteAddress() {
        return Optional.ofNullable(RequestContextHolder.getRequestAttributes())
                .filter(ServletRequestAttributes.class::isInstance)
                .map(ServletRequestAttributes.class::cast)
                .map(ServletRequestAttributes::getRequest)
                .map(HttpServletRequest::getRemoteAddr)
                .orElse("unknown");
    }
}
