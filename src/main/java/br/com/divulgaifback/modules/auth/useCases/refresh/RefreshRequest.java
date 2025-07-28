package br.com.divulgaifback.modules.auth.useCases.refresh;

import jakarta.validation.constraints.NotBlank;

public record RefreshRequest(
        @NotBlank(message = "{refresh.refreshToken.required}") String refreshToken
) {
    public static RefreshRequest toDomain(RefreshRequest input) {
        return new RefreshRequest(input.refreshToken);
    }
}
