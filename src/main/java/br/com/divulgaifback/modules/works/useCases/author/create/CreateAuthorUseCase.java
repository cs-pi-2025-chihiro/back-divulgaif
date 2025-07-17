package br.com.divulgaifback.modules.works.useCases.author.create;

import br.com.divulgaifback.common.constants.AuthorConstants;
import br.com.divulgaifback.common.exceptions.custom.DuplicateException;
import br.com.divulgaifback.modules.users.entities.Author;
import br.com.divulgaifback.modules.users.entities.User;
import br.com.divulgaifback.modules.users.repositories.AuthorRepository;
import br.com.divulgaifback.modules.users.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CreateAuthorUseCase {
    private final AuthorRepository authorRepository;
    private final UserRepository userRepository;
    private final CreateAuthorResponse createAuthorResponse;

    @Transactional
    public CreateAuthorResponse execute(CreateAuthorRequest request) {
        authorRepository.findByEmail(request.email()).ifPresent(author -> {
            throw DuplicateException.with(Author.class, "email", request.email());
        });

        Author author = new Author();
        author.setName(request.name());
        author.setEmail(request.email());

        Optional<User> userOptional = userRepository.findByEmail(request.email());
        if (userOptional.isPresent()) {
            author.setUser(userOptional.get());
            author.setType(AuthorConstants.REGISTERED_AUTHOR);
        } else {
            author.setType(AuthorConstants.UNREGISTERED_AUTHOR);
        }

        Author savedAuthor = authorRepository.save(author);
        return createAuthorResponse.toPresentation(savedAuthor);
    }
}