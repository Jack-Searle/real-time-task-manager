package com.jacksearle.backend.auth;

import com.jacksearle.backend.auth.dto.AuthResponse;
import com.jacksearle.backend.auth.dto.LoginRequest;
import com.jacksearle.backend.auth.dto.RegisterRequest;
import com.jacksearle.backend.auth.dto.RegisterResponse;
import com.jacksearle.backend.auth.dto.ResendVerificationRequest;
import com.jacksearle.backend.common.exception.BadRequestException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController()
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final String frontendUrl;

    public AuthController(
            AuthService authService,
            @Value("${app.frontend-url}") String frontendUrl
    ) {
        this.authService = authService;
        this.frontendUrl = frontendUrl;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisterResponse register(@Valid @RequestBody RegisterRequest registerRequest) {
        return authService.register(registerRequest);
    }

    @GetMapping("/verify-email")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void verifyEmail(@RequestParam String token) {
        authService.verifyEmail(token);
    }

    @GetMapping("/verify-email/redirect")
    public ResponseEntity<Void> verifyEmailAndRedirect(@RequestParam String token) {
        String loginPath = "/login?verified=1";
        try {
            authService.verifyEmail(token);
        } catch (BadRequestException ex) {
            loginPath = "/login?verification=invalid";
        }

        URI loginUri = URI.create(frontendUrl.replaceAll("/+$", "") + loginPath);
        return ResponseEntity.status(HttpStatus.FOUND).location(loginUri).build();
    }

    @PostMapping("/resend-verification")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        authService.resendVerification(request.getEmail());
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest loginRequest) {
        return authService.login(loginRequest.getEmail(), loginRequest.getPassword());
    }

}
