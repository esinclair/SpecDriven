package com.example.specdriven.feature;

import com.example.specdriven.api.model.ErrorResponse;
import com.example.specdriven.config.FeatureFlagProperties;
import com.example.specdriven.error.ErrorCode;
import com.example.specdriven.error.ErrorResponseFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Helper to gate Users API endpoints behind the usersApi feature flag.
 * When disabled, returns 404 with shared error body.
 */
@Component
public class UsersApiFeatureGate {

    private final FeatureFlagProperties featureFlags;

    public UsersApiFeatureGate(FeatureFlagProperties featureFlags) {
        this.featureFlags = featureFlags;
    }

    /**
     * Check if the Users API feature is enabled.
     */
    public boolean isEnabled() {
        return featureFlags.isUsersApi();
    }

    /**
     * Get a 404 error response for disabled feature.
     */
    public ResponseEntity<ErrorResponse> featureDisabledResponse(String path) {
        ErrorResponse error = ErrorResponseFactory.from(
                ErrorCode.FEATURE_DISABLED,
                "Feature not available",
                Map.of("path", path, "feature", "usersApi")
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * Execute action if feature is enabled, otherwise return 404.
     */
    public <T> ResponseEntity<T> ifEnabled(String path, java.util.function.Supplier<ResponseEntity<T>> action) {
        if (!isEnabled()) {
            @SuppressWarnings("unchecked")
            ResponseEntity<T> response = (ResponseEntity<T>) (ResponseEntity<?>) featureDisabledResponse(path);
            return response;
        }
        return action.get();
    }
}

