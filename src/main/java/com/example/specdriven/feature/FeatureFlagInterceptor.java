package com.example.specdriven.feature;

import com.example.specdriven.config.FeatureFlagProperties;
import com.example.specdriven.error.FeatureDisabledException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor that checks feature flags before allowing requests to proceed.
 * Throws FeatureDisabledException (404) when a feature is disabled.
 */
@Component
public class FeatureFlagInterceptor implements HandlerInterceptor {

    private final FeatureFlagProperties featureFlags;

    public FeatureFlagInterceptor(FeatureFlagProperties featureFlags) {
        this.featureFlags = featureFlags;
    }

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) {
        String path = request.getRequestURI();

        // Check if this is a users API endpoint
        if (path.startsWith("/users") || path.startsWith("/login")) {
            if (!featureFlags.isUsersApi()) {
                throw new FeatureDisabledException("Feature not available");
            }
        }

        return true;
    }
}
