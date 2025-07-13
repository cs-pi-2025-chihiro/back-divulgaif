package br.com.divulgaifback.modules.works.useCases.author.list;

import br.com.divulgaifback.modules.users.entities.Author;
import br.com.divulgaifback.modules.users.repositories.AuthorRepository;
import br.com.divulgaifback.modules.works.entities.Label;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListAuthorsUseCase {
    private AuthorRepository authorRepository;
    private ListAuthorsResponse listAuthorsResponse;

    @Transactional(readOnly = true)
    public Page<ListAuthorsResponse> execute(BooleanBuilder operators, Predicate predicate, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder(predicate);
        if (operators.hasValue()) builder.and(operators);
        Page<Author> authors = authorRepository.findAll(builder, pageable);
        return authors.map(listAuthorsResponse::toPresentation);
    }
}
