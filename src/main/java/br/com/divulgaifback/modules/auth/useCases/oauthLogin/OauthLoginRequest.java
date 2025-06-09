package br.com.divulgaifback.modules.auth.useCases.oauthLogin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OauthLoginRequest(
        @NotNull(message = "userData is required")
        @Valid
        UserData userData,

        @NotBlank(message = "provider is required")
        String provider
) {
    public record UserData(
            @NotBlank(message = "identificacao is required") String identificacao,
            @NotBlank(message = "nome is required") String nome,
            @NotBlank(message = "email is required") String email,
            @NotBlank(message = "tipoUsuario is required") String tipoUsuario
    ) {}
}
