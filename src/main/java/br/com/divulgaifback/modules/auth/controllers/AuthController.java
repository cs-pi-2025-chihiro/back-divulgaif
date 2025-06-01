package br.com.divulgaifback.modules.auth.controllers;

import br.com.divulgaifback.modules.auth.services.AuthService;
import br.com.divulgaifback.modules.auth.useCases.login.LoginRequest;
import br.com.divulgaifback.modules.auth.useCases.login.LoginResponse;
import br.com.divulgaifback.modules.auth.useCases.refresh.RefreshRequest;
import br.com.divulgaifback.modules.auth.useCases.refresh.RefreshResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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
}
