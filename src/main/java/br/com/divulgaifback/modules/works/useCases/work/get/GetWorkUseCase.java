package br.com.divulgaifback.modules.works.useCases.work.get;

import br.com.divulgaifback.common.exceptions.custom.NotFoundException;
import br.com.divulgaifback.modules.works.entities.Work;
import br.com.divulgaifback.modules.works.repositories.WorkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetWorkUseCase {
    private WorkRepository workRepository;
    private GetWorkResponse getWorkResponse;

    @Transactional(readOnly = true)
    public GetWorkResponse execute(Integer workId) {
        Work work = workRepository.findById(workId).orElseThrow(() -> NotFoundException.with(Work.class, "id", workId.toString()));
        return getWorkResponse.toPresentation(work);
    }
}
