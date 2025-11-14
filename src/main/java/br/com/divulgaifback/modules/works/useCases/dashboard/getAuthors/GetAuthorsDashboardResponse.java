package br.com.divulgaifback.modules.works.useCases.dashboard.getAuthors;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
public class GetAuthorsDashboardResponse {
    Long internalAuthorsCount;
    Long externalAuthorsCount;
    String mostCitedAuthor;
}
