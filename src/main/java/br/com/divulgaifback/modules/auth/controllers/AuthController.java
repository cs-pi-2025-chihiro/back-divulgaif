package br.com.divulgaifback.modules.auth.controllers;

import br.com.divulgaifback.modules.auth.services.AuthService;
import br.com.divulgaifback.modules.auth.useCases.forgotPassword.ForgotPasswordRequest;
import br.com.divulgaifback.modules.auth.useCases.forgotPassword.ForgotPasswordResponse;
import br.com.divulgaifback.modules.auth.useCases.login.LoginRequest;
import br.com.divulgaifback.modules.auth.useCases.login.LoginResponse;
import br.com.divulgaifback.modules.auth.useCases.oauthLogin.OauthLoginRequest;
import br.com.divulgaifback.modules.auth.useCases.refresh.RefreshRequest;
import br.com.divulgaifback.modules.auth.useCases.refresh.RefreshResponse;
import br.com.divulgaifback.modules.auth.useCases.resetPassword.ResetPasswordRequest;
import br.com.divulgaifback.modules.auth.useCases.resetPassword.ResetPasswordResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public LoginResponse login(@Valid @RequestBody LoginRequest loginRequest) {
        return authService.login(loginRequest);
    }

    @PostMapping("/refresh-token")
    @ResponseStatus(HttpStatus.OK)
    public RefreshResponse refresh(@Valid @RequestBody RefreshRequest refreshTokenRequest) {
        return authService.refresh(refreshTokenRequest);
    }

    @PostMapping("/oauth-login")
    @ResponseStatus(HttpStatus.OK)
    public LoginResponse oauthLogin(@Valid @RequestBody OauthLoginRequest oauthLoginRequest) {
        return authService.oauthLogin(oauthLoginRequest);
    }

    @PostMapping("/forgot-password")
    @ResponseStatus(HttpStatus.OK)
    public ForgotPasswordResponse forgotPassword(@Valid @RequestBody ForgotPasswordRequest request, Locale locale) {
        return authService.forgotPassword(request, locale);
    }

    @PostMapping("/reset-password")
    @ResponseStatus(HttpStatus.OK)
    public ResetPasswordResponse resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        return authService.resetPassword(request);
    }
}
