package com.example.specdriven.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Feature flags for the application.
 * All flags default to false (disabled).
 */
@Component
@ConfigurationProperties(prefix = "feature-flag")
public class FeatureFlagProperties {

    private boolean usersApi = false;

    public boolean isUsersApi() {
        return usersApi;
    }

    public void setUsersApi(boolean usersApi) {
        this.usersApi = usersApi;
    }
}

