package br.com.divulgaifback.modules.works.useCases.label.create;

import br.com.divulgaifback.common.exceptions.custom.NotFoundException;
import br.com.divulgaifback.modules.works.entities.Label;
import br.com.divulgaifback.modules.works.entities.Work;
import br.com.divulgaifback.modules.works.repositories.LabelRepository;
import br.com.divulgaifback.modules.works.repositories.WorkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateLabelUseCase {
    private LabelRepository labelRepository;
    private WorkRepository workRepository;
    private CreateLabelResponse createLabelResponse;

    @Transactional
    public CreateLabelResponse execute(CreateLabelRequest request) {
        Label label = CreateLabelRequest.toDomain(request);
        Work work = workRepository.findById(request.workId()).orElseThrow(() -> NotFoundException.with(Work.class, "id", request.workId()));
        label.getWorks().add(work);
        Label savedLabel = labelRepository.save(label);
        return createLabelResponse.toPresentation(savedLabel);
    }
}
