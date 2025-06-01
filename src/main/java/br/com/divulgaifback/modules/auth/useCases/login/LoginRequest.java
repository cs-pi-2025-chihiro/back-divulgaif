package br.com.divulgaifback.modules.auth.useCases.login;

import br.com.divulgaifback.modules.users.entities.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank(message = "Username/Email is required")
        String identifier,

        @NotBlank(message = "Password is required")
        String password
) {

    @JsonIgnore
    public boolean isSuapLogin() {
        // apenas números
        return identifier != null && identifier.matches("\\d+");
    }

    @JsonIgnore
    public boolean isEmailLogin() {
        return identifier != null && identifier.contains("@");
    }

    public static User toDomain(LoginRequest input) {
        final var user = new User();
        if (input.isEmailLogin()) {
            user.setEmail(input.identifier);
        } else if (input.isSuapLogin()) {
            user.setRa(input.identifier);
        }
        user.setPassword(input.password);
        return user;
    }
}