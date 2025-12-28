package com.example.specdriven.config;

import com.example.specdriven.feature.FeatureFlagInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC configuration for interceptors and other web-layer concerns.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final FeatureFlagInterceptor featureFlagInterceptor;

    public WebMvcConfig(FeatureFlagInterceptor featureFlagInterceptor) {
        this.featureFlagInterceptor = featureFlagInterceptor;
    }

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        // Register feature flag interceptor for users API endpoints
        registry.addInterceptor(featureFlagInterceptor)
                .addPathPatterns("/users/**", "/login");
    }
}
