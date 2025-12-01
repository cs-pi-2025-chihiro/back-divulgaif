package br.com.divulgaifback.modules.works.useCases.author.getAll;

import br.com.divulgaifback.modules.users.entities.Author;
import br.com.divulgaifback.modules.users.entities.QAuthor;
import br.com.divulgaifback.modules.users.repositories.AuthorRepository;
import br.com.divulgaifback.modules.works.useCases.author.list.ListAuthorsResponse;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class GetAllAuthorsUseCase {
    private final AuthorRepository authorRepository;
    private final ListAuthorsResponse listAuthorsResponse;

    @Transactional(readOnly = true)
    public Page<ListAuthorsResponse> execute(BooleanBuilder operators, Predicate predicate, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder(predicate);
        if (operators.hasValue()) builder.and(operators);
        Page<Author> authorsFromDb = authorRepository.findAll(builder, pageable);
        return authorsFromDb.map(listAuthorsResponse::toPresentation);
    }
}

