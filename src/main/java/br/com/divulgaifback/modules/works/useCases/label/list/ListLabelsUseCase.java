package br.com.divulgaifback.modules.works.useCases.label.list;

import br.com.divulgaifback.modules.works.entities.Label;
import br.com.divulgaifback.modules.works.repositories.LabelRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListLabelsUseCase {
    private final LabelRepository labelRepository;
    private final ListLabelsResponse listLabelsResponse;

    @Transactional(readOnly = true)
    public Page<ListLabelsResponse> execute(BooleanBuilder operators, Predicate predicate, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder(predicate);
        if (operators.hasValue() && operators.hasValue()) builder.and(operators);
        Page<Label> labels = labelRepository.findAll(builder, pageable);
        return labels.map(listLabelsResponse::toPresentation);
    }
}
