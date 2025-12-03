package br.com.divulgaifback.modules.auth.useCases.forgotPassword;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequest(
        @NotBlank(message = "{forgot.password.email.required}")
        @Email(message = "{forgot.password.email.invalid}")
        String email
) {}

