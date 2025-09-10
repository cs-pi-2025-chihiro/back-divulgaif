package br.com.divulgaifback.modules.users.useCases.user.create;

import br.com.divulgaifback.modules.users.entities.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateUserRequest(
        @NotBlank(message = "{createuser.name.required}") String name,
        @Email @NotBlank(message = "{createuser.email.required}") String email,
        @Email String secondaryEmail,
        @NotBlank(message = "{createuser.ra.required}") String ra,
        @Pattern(regexp = "\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}", message = "{createuser.cpf.pattern}")
        String cpf,
        String bio,
        String password,
        String phone,
        String avatarUrl,
        String userType
) {
    public static User toDomain(CreateUserRequest request) {
        final var user = new User();
        user.setName(request.name);
        user.setEmail(request.email);
        user.setSecondaryEmail(request.secondaryEmail);
        user.setRa(request.ra);
        user.setCpf(request.cpf);
        user.setBio(request.bio);
        user.setPassword(request.password);
        user.setPhone(request.phone);
        user.setAvatarUrl(request.avatarUrl);
        user.setUserType(request.userType);

        return user;
    }
}
