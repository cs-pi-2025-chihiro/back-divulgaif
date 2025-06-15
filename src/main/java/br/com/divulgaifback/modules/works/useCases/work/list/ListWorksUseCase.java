package br.com.divulgaifback.modules.works.useCases.work.list;

import br.com.divulgaifback.modules.works.entities.Work;
import br.com.divulgaifback.modules.works.repositories.WorkRepository;
import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListWorksUseCase {
    private final WorkRepository workRepository;
    private final ListWorksResponse listWorksResponse;

    @Transactional(readOnly = true)
    public Page<ListWorksResponse> execute(BooleanBuilder operators, Predicate predicate, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder(predicate);
        if (operators.hasValue()) builder.and(operators);
        Page<Work> works = workRepository.findAll(builder, pageable);
        return works.map(listWorksResponse::toPresentation);
    }
}
