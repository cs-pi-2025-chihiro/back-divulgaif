package br.com.divulgaifback.modules.works.useCases.dashboard.getLabels;

import br.com.divulgaifback.common.exceptions.custom.NotFoundException;
import br.com.divulgaifback.modules.works.entities.Label;
import br.com.divulgaifback.modules.works.entities.enums.WorkStatusEnum;
import br.com.divulgaifback.modules.works.repositories.LabelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetLabelsDashboardUseCase {
    private final LabelRepository labelRepository;
    private static final String PUBLISHED_WORK_STATUS = WorkStatusEnum.PUBLISHED.name();

    @Secured({"IS_ADMIN", "IS_TEACHER"})
    @Transactional(readOnly = true)
    public GetLabelsDashboardResponse execute() {
        GetLabelsDashboardResponse response = new GetLabelsDashboardResponse();

        Pageable topOne = PageRequest.of(0, 1);
        Long quantityOfLabels = labelRepository.countAllLabelsByWorkStatus(PUBLISHED_WORK_STATUS);
        Page<Label> mostUsedLabel = labelRepository.findMostUsedLabel(topOne, PUBLISHED_WORK_STATUS);
        Page<Label> leastUsedLabel = labelRepository.findLeastUsedLabel(topOne, PUBLISHED_WORK_STATUS);

        String mostUsedName = mostUsedLabel.stream().findFirst()
                .orElseThrow(() -> NotFoundException.with(Label.class, "Most Used Label", topOne)).getName();

        String leastUsedName = leastUsedLabel.stream().findFirst()
                .orElseThrow(() -> NotFoundException.with(Label.class, "Most Used Label", topOne)).getName();

        response.setQuantityOfLabels(quantityOfLabels);
        response.setMostUsedLabel(mostUsedName);
        response.setLeastUsedLabel(leastUsedName);

        return response;
    }
}
