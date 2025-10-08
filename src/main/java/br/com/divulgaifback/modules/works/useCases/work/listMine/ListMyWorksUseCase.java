package br.com.divulgaifback.modules.works.useCases.work.listMine;

import br.com.divulgaifback.modules.auth.services.AuthService;
import br.com.divulgaifback.modules.works.entities.QWork;
import br.com.divulgaifback.modules.works.entities.Work;
import br.com.divulgaifback.modules.works.entities.enums.WorkStatusEnum;
import br.com.divulgaifback.modules.works.repositories.WorkRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ListMyWorksUseCase {
    private final WorkRepository workRepository;
    private final ListMyWorksResponse listMyWorksResponse;

    @Transactional(readOnly = true)
    public Page<ListMyWorksResponse> execute(BooleanBuilder operators, Predicate predicate, Pageable pageable, String search) {
        QWork qWork = QWork.work;
        BooleanBuilder builder = new BooleanBuilder(predicate);

        int myId = AuthService.getUserFromToken().getId();
        String[] worksToList = new String[]{WorkStatusEnum.DRAFT.name(), WorkStatusEnum.PENDING_CHANGES.name()};

        builder.and(qWork.authors.any().user.id.eq(myId));
        builder.and(qWork.workStatus.name.in(worksToList));

        if (operators.hasValue()) builder.and(operators);

        if (Objects.nonNull(search) && !search.trim().isEmpty()) {
            String searchTerm = search.trim();
            Predicate searchPredicate = qWork.title.containsIgnoreCase(searchTerm)
                    .or(qWork.description.containsIgnoreCase(searchTerm));
            builder.and(searchPredicate);
        }

        Page<Work> works = workRepository.findAll(builder, pageable);
        return works.map(listMyWorksResponse::toPresentation);
    }
}