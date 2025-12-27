package com.example.specdriven.security;

import com.example.specdriven.users.persistence.UserRepository;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

/**
 * Authorization manager that allows unauthenticated POST /users only when zero users exist.
 * This enables bootstrap creation of the first admin user.
 */
@Component
@ConditionalOnProperty(name = "feature-flag.users-api", havingValue = "true")
public class BootstrapCreateUserAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private final UserRepository userRepository;

    public BootstrapCreateUserAuthorizationManager(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authentication, RequestAuthorizationContext context) {
        Authentication auth = authentication.get();

        // If authenticated, allow
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return new AuthorizationDecision(true);
        }

        // If not authenticated, allow only if user count is 0 (bootstrap mode)
        long userCount = userRepository.count();
        return new AuthorizationDecision(userCount == 0);
    }
}

