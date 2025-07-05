package br.com.divulgaifback.modules.works.useCases.link.list;

import br.com.divulgaifback.modules.works.entities.Label;
import br.com.divulgaifback.modules.works.entities.Link;
import br.com.divulgaifback.modules.works.repositories.LinkRepository;
import br.com.divulgaifback.modules.works.useCases.label.list.ListLabelsResponse;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListLinksUseCase {
    private LinkRepository linkRepository;
    private ListLinksResponse listLinksResponse;

    @Transactional(readOnly = true)
    public Page<ListLinksResponse> execute(BooleanBuilder operators, Predicate predicate, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder(predicate);
        if (operators.hasValue()) builder.and(operators);
        Page<Link> links = linkRepository.findAll(builder, pageable);
        return links.map(listLinksResponse::toPresentation);
    }
}
