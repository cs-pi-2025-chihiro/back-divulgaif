package br.com.divulgaifback.modules.auth.useCases.oauthLogin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OauthLoginRequest(
        @NotNull(message = "{oauth.userdata.required}")
        @Valid
        UserData userData,

        @NotBlank(message = "{oauth.provider.required}")
        String provider
) {
    public record UserData(
            @NotBlank(message = "{oauth.userdata.identificacao.required}")
            String identificacao,
            @NotBlank(message = "{oauth.userdata.nome.required}")
            String nome,
            @NotBlank(message = "{oauth.userdata.email.required}")
            String email,
            @NotBlank(message = "{oauth.userdata.tipousuario.required}")
            String tipoUsuario
    ) {}
}