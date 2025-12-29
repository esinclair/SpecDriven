package com.example.specdriven.service;

import com.example.specdriven.config.FeatureFlagConfig;
import org.springframework.stereotype.Service;

/**
 * Service for checking feature flag status.
 */
@Service
public class FeatureFlagService {
    
    private final FeatureFlagConfig featureFlagConfig;
    
    public FeatureFlagService(FeatureFlagConfig featureFlagConfig) {
        this.featureFlagConfig = featureFlagConfig;
    }
    
    public boolean isUsersApiEnabled() {
        return featureFlagConfig.isUsersApi();
    }
}
