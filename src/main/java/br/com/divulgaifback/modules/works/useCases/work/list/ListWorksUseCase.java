package br.com.divulgaifback.modules.works.useCases.work.list;

import br.com.divulgaifback.modules.works.entities.QWork;
import br.com.divulgaifback.modules.works.entities.Work;
import br.com.divulgaifback.modules.works.repositories.WorkRepository;
import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public Page<ListWorksResponse> execute(BooleanBuilder operators, Predicate predicate, Pageable pageable,
            String search) {
        BooleanBuilder builder = new BooleanBuilder(predicate);
        if (operators != null && operators.hasValue()) builder.and(operators);
        

        if (search != null && !search.trim().isEmpty()) {
            String searchTerm = search.trim();            

            QWork qWork = QWork.work;
            Predicate searchPredicate = qWork.title.containsIgnoreCase(searchTerm)
                    .or(qWork.description.containsIgnoreCase(searchTerm));

            builder.and(searchPredicate);
           
        } 
        Page<Work> works = workRepository.findAll(builder, pageable);
        return works.map(listWorksResponse::toPresentation);
    }
}