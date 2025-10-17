package br.com.divulgaifback.modules.works.useCases.author.update;

import br.com.divulgaifback.modules.users.entities.Author;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UpdateAuthorRequest(
    @NotBlank(message = "{author.name.required}") String name,
    @Email @NotBlank(message = "{author.email.required}") String email
) {
    public static Author toDomain(UpdateAuthorRequest request, Author existingAuthor) {
        existingAuthor.setName(request.name());
        existingAuthor.setEmail(request.email());
        return existingAuthor;
    }
} 
