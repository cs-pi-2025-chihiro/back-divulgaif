package br.com.divulgaifback.modules.works.useCases.author.delete;

import br.com.divulgaifback.common.exceptions.custom.NotFoundException;
import br.com.divulgaifback.modules.users.entities.Author;
import br.com.divulgaifback.modules.users.repositories.AuthorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeleteAuthorUseCase {
    private final AuthorRepository authorRepository;

    @Secured({"IS_ADMIN", "IS_TEACHER"})
    @Transactional
    public void execute(Integer authorId) {
        Author author = authorRepository.findById(authorId)
                .orElseThrow(() -> NotFoundException.with(Author.class, "id", authorId));
        
        authorRepository.delete(author);
    }
}
