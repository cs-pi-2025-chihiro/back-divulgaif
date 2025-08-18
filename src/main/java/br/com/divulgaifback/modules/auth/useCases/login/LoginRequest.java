package br.com.divulgaifback.modules.auth.useCases.login;

import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "{login.identifier.required}")
        String identifier,

        @NotBlank(message = "{login.password.required}")
        String password
) {}