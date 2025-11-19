package br.com.divulgaifback.modules.works.useCases.author.update;

import br.com.divulgaifback.modules.users.entities.Author;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpdateAuthorRequest(
    @NotBlank(message = "{author.name.required}") String name,
    @Email String email,
    String type
) {
    public static void toDomain(UpdateAuthorRequest request, Author author) {
        author.setName(request.name());
        author.setEmail(request.email());
        author.setType(request.type());
    }
}

