package br.com.divulgaifback.modules.works.useCases.dashboard.getAuthors;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetAuthorsDashboardUseCase {

    @Transactional(readOnly = true)
    public GetAuthorsDashboardResponse execute(GetAuthorsDashboardRequest request) {
        return new GetAuthorsDashboardResponse();
    }
}
