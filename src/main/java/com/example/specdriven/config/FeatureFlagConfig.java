package com.example.specdriven.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration properties for feature flags.
 */
@Configuration
@ConfigurationProperties(prefix = "featureflag")
public class FeatureFlagConfig {
    
    private boolean usersApi = false;  // Default: disabled
    
    public boolean isUsersApi() {
        return usersApi;
    }
    
    public void setUsersApi(boolean usersApi) {
        this.usersApi = usersApi;
    }
}
