package br.com.divulgaifback.modules.works.useCases.author.list;

import br.com.divulgaifback.modules.users.entities.Author;
import br.com.divulgaifback.modules.users.repositories.AuthorRepository;
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
public class ListAuthorsUseCase {
    private final AuthorRepository authorRepository;
    private final ListAuthorsResponse listAuthorsResponse;

    @Transactional(readOnly = true)
    public Page<ListAuthorsResponse> execute(BooleanBuilder operators, Predicate predicate, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder(predicate);
        if (operators.hasValue()) builder.and(operators);

        Page<Author> authorsFromDb = authorRepository.findAll(builder, pageable);
        List<Author> originalAuthorList = authorsFromDb.getContent();

        Map<String, Author> distinctAuthorsMap = new HashMap<>();
        for (Author author : originalAuthorList) distinctAuthorsMap.putIfAbsent(author.getEmail(), author);

        List<Author> uniqueAuthors = new ArrayList<>(distinctAuthorsMap.values());
        Page<Author> uniqueAuthorsPage = new PageImpl<>(uniqueAuthors, pageable, authorsFromDb.getTotalElements());
        return uniqueAuthorsPage.map(listAuthorsResponse::toPresentation);
    }
}