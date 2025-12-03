package br.com.divulgaifback.modules.auth.useCases.resetPassword;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequest(
        @NotBlank(message = "{reset.password.token.required}")
        String token,

        @NotBlank(message = "{reset.password.password.required}")
        @Size(min = 8, message = "{reset.password.password.min}")
        String password
) {}

