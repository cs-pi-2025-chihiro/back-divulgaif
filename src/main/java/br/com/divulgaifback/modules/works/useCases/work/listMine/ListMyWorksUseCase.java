package br.com.divulgaifback.modules.works.useCases.work.listMine;

import br.com.divulgaifback.modules.auth.services.AuthService;
import br.com.divulgaifback.modules.works.entities.QWork;
import br.com.divulgaifback.modules.works.entities.Work;
import br.com.divulgaifback.modules.works.entities.WorkStatus;
import br.com.divulgaifback.modules.works.entities.enums.WorkStatusEnum;
import br.com.divulgaifback.modules.works.repositories.WorkRepository;
import br.com.divulgaifback.modules.works.repositories.WorkStatusRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListMyWorksUseCase {
    private final WorkRepository workRepository;
    private final ListMyWorksResponse listMyWorksResponse;

    @Transactional(readOnly = true)
    public Page<ListMyWorksResponse> execute(BooleanBuilder operators, Predicate predicate, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder(predicate);
        String[] worksToList = new String[]{WorkStatusEnum.DRAFT.name(), WorkStatusEnum.PENDING_CHANGES.name()};
        if (operators.hasValue()) builder.and(operators);

        int myId = AuthService.getUserFromToken().getId();
        Page<Work> works = workRepository.findMyWorks(worksToList, myId, pageable);
        return works.map(listMyWorksResponse::toPresentation);
    }
}
