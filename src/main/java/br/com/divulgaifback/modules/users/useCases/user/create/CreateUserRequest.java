package br.com.divulgaifback.modules.users.useCases.user.create;

import br.com.divulgaifback.modules.users.entities.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateUserRequest(
        @NotBlank(message = "Name is required") String name,
        @Email @NotBlank(message = "Email is required") String email,
        @Email String secondaryEmail,
        @NotBlank(message = "RA is required") String ra,
        @Pattern(regexp = "\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}", message = "CPF must be in the format xxx.xxx.xxx-xx")
        String cpf,
        String bio,
        @NotBlank(message = "Password is required") String password,
        String phone,
        String avatarUrl,
        String userType
) {
    public static User toDomain(CreateUserRequest input) {
        final var user = new User();
        user.setName(input.name);
        user.setEmail(input.email);
        user.setSecondaryEmail(input.secondaryEmail);
        user.setRa(input.ra);
        user.setCpf(input.cpf);
        user.setBio(input.bio);
        user.setPassword(input.password);
        user.setPhone(input.phone);
        user.setAvatarUrl(input.avatarUrl);
        user.setUserType(input.userType);

        return user;
    }
}
