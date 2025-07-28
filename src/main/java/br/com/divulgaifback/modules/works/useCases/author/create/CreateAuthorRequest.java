package br.com.divulgaifback.modules.works.useCases.author.create;

import br.com.divulgaifback.modules.users.entities.Author;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateAuthorRequest(
    @NotBlank(message = "{author.name.required}") String name,
    @Email @NotBlank(message = "{author.email.required}") String email
) {
    public static Author toDomain(CreateAuthorRequest request) {
        Author author = new Author();
        author.setName(request.name());
        author.setEmail(request.email());
        return author;
    }
}