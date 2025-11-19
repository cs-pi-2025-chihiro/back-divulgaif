package br.com.divulgaifback.modules.works.useCases.author.update;

import br.com.divulgaifback.common.exceptions.custom.DuplicateException;
import br.com.divulgaifback.common.exceptions.custom.NotFoundException;
import br.com.divulgaifback.modules.users.entities.Author;
import br.com.divulgaifback.modules.users.repositories.AuthorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UpdateAuthorUseCase {
    private final AuthorRepository authorRepository;
    private final UpdateAuthorResponse updateAuthorResponse;

    @Transactional
    @Secured({"IS_ADMIN", "IS_TEACHER"})
    public UpdateAuthorResponse execute(Integer authorId, UpdateAuthorRequest request) {
        Author author = authorRepository.findById(authorId)
                .orElseThrow(() -> NotFoundException.with(Author.class, "id", authorId));

        if (!author.getEmail().equals(request.email())) {
            Optional<Author> existingAuthor = authorRepository.findByEmail(request.email());
            if (existingAuthor.isPresent() && !existingAuthor.get().getId().equals(authorId)) {
                throw DuplicateException.with(Author.class, "email", request.email());
            }
        }

        UpdateAuthorRequest.toDomain(request, author);
        Author updatedAuthor = authorRepository.save(author);
        
        return updateAuthorResponse.toPresentation(updatedAuthor);
    }
}