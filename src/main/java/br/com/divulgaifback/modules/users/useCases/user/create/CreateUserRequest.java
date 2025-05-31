package br.com.divulgaifback.modules.users.useCases.user.create;

import br.com.divulgaifback.modules.users.entities.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.br.CPF;

public record CreateUserRequest(
        @NotBlank(message = "Name is required") String name,
        @Email @NotBlank(message = "Email is required") String email,
        @NotBlank(message = "RA is required") String ra,
        @CPF @NotBlank(message = "CPF is required") String cpf,
        String bio,
        @NotBlank(message = "Password is required") String password,
        @NotBlank(message = "Phone is required") String phone,
        String avatarUrl
) {
    public static User toDomain(CreateUserRequest input) {
        final var user = new User();
        user.setName(input.name);
        user.setEmail(input.email);
        user.setRa(input.ra);
        user.setCpf(input.cpf != null ? input.cpf.replaceAll("\\D", "") : null);
        user.setBio(input.bio);
        user.setPassword(input.password);
        user.setPhone(input.phone);
        user.setAvatarUrl(input.avatarUrl);

        return user;
    }
}
