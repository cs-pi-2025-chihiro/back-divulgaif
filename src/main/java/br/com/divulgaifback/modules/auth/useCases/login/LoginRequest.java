package br.com.divulgaifback.modules.auth.useCases.login;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Username/Email is required")
        String identifier,

        @NotBlank(message = "Password is required")
        String password
) {}