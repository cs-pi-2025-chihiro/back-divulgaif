package br.com.divulgaifback.modules.works.useCases.dashboard.getAuthors;

import br.com.divulgaifback.common.exceptions.custom.NotFoundException;
import br.com.divulgaifback.modules.users.entities.Author;
import br.com.divulgaifback.modules.users.repositories.AuthorRepository;
import br.com.divulgaifback.modules.works.entities.enums.WorkStatusEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetAuthorsDashboardUseCase {
    private final AuthorRepository authorRepository;
    private static final String PUBLISHED_WORK_STATUS = WorkStatusEnum.PUBLISHED.name();

    @Secured({"IS_ADMIN", "IS_TEACHER"})
    @Transactional(readOnly = true)
    public GetAuthorsDashboardResponse execute() {
        GetAuthorsDashboardResponse response = new GetAuthorsDashboardResponse();

        long internalUsers = authorRepository.countAllByUserExists(PUBLISHED_WORK_STATUS);
        long externalUsers = authorRepository.count() - internalUsers;
        Pageable topOne = PageRequest.of(0, 1);
        Author mostCitedAuthor = authorRepository.findMostCited(topOne, PUBLISHED_WORK_STATUS).stream().findFirst()
                .orElseThrow(() -> NotFoundException.with(Author.class, "Most Cited Author", topOne));

        response.setInternalAuthorsCount(internalUsers);
        response.setExternalAuthorsCount(externalUsers);
        response.setMostCitedAuthor(mostCitedAuthor.getName());
        return response;
    }
}
