package com.example.specdriven.users;

import com.example.specdriven.api.LoginApi;
import com.example.specdriven.api.model.LoginRequest;
import com.example.specdriven.api.model.LoginResponse;
import com.example.specdriven.feature.UsersApiFeatureGate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Login API controller implementing generated OpenAPI interface.
 * Gated by feature-flag.users-api feature flag.
 * T036: Login controller implementation.
 */
@RestController
@ConditionalOnProperty(name = "feature-flag.users-api", havingValue = "true")
public class LoginController implements LoginApi {

    private final UsersApiFeatureGate featureGate;
    private final LoginService loginService;

    public LoginController(UsersApiFeatureGate featureGate, LoginService loginService) {
        this.featureGate = featureGate;
        this.loginService = loginService;
    }

    @Override
    public ResponseEntity<LoginResponse> login(LoginRequest loginRequest) {
        return featureGate.ifEnabled("/login", () -> {
            LoginResponse response = loginService.login(loginRequest);
            return ResponseEntity.ok(response);
        });
    }
}
