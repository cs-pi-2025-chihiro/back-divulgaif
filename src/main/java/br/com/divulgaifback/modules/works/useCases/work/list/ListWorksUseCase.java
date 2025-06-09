package br.com.divulgaifback.modules.works.useCases.work.list;

import lombok.RequiredArgsConstructor;
import com.querydsl.core.types.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListWorksUseCase {

    @Transactional(readOnly = true)
    public Page<ListWorksResponse> execute(Predicate predicate, Pageable pageable) {
        return null;
    }
}
