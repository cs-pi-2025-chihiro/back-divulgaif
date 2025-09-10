package br.com.divulgaifback.modules.works.useCases.work.listMine;

import br.com.divulgaifback.modules.auth.services.AuthService;
import br.com.divulgaifback.modules.works.entities.QWork;
import br.com.divulgaifback.modules.works.entities.Work;
import br.com.divulgaifback.modules.works.repositories.WorkRepository;
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
        if (operators.hasValue()) builder.and(operators);

        int myId = AuthService.getUserFromToken().getId();
        builder.and(QWork.work.authors.any().user.id.eq(myId));
        Page<Work> works = workRepository.findAll(builder, pageable);
        return works.map(listMyWorksResponse::toPresentation);
    }
}
