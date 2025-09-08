package br.com.divulgaifback.modules.works.useCases.work.reject;

import br.com.divulgaifback.common.exceptions.custom.NotFoundException;
import br.com.divulgaifback.modules.works.entities.Work;
import br.com.divulgaifback.modules.works.entities.WorkStatus;
import br.com.divulgaifback.modules.works.entities.enums.WorkStatusEnum;
import br.com.divulgaifback.modules.works.repositories.WorkRepository;
import br.com.divulgaifback.modules.works.repositories.WorkStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RejectWorkUseCase {
    private final WorkRepository workRepository;
    private final WorkStatusRepository workStatusRepository;

    @Transactional
    public void execute(Integer workId) {
        Work work = workRepository.findById(workId).orElseThrow(() -> NotFoundException.with(Work.class, "id", workId));
        WorkStatus publishedStatus = workStatusRepository.findById(WorkStatusEnum.REJECTED.getId()).orElseThrow(() -> NotFoundException.with(WorkStatus.class, "id", WorkStatusEnum.REJECTED.getId()));
        work.setWorkStatus(publishedStatus);
        workRepository.save(work);
    }
}
