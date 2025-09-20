package br.com.divulgaifback.modules.works.useCases.work.publish;

import br.com.divulgaifback.common.exceptions.custom.NotFoundException;
import br.com.divulgaifback.modules.works.entities.Work;
import br.com.divulgaifback.modules.works.entities.WorkStatus;
import br.com.divulgaifback.modules.works.entities.enums.WorkStatusEnum;
import br.com.divulgaifback.modules.works.repositories.WorkRepository;
import br.com.divulgaifback.modules.works.repositories.WorkStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PublishWorkUseCase {
    private final WorkRepository workRepository;
    private final WorkStatusRepository workStatusRepository;

    @Secured({"IS_ADMIN", "IS_TEACHER"})
    @Transactional
    public void execute(Integer workId) {
        Work work = workRepository.findById(workId).orElseThrow(() -> NotFoundException.with(Work.class, "id", workId));
        WorkStatus publishedStatus = workStatusRepository.findById(WorkStatusEnum.PUBLISHED.getId()).orElseThrow(() -> NotFoundException.with(WorkStatus.class, "id", WorkStatusEnum.PUBLISHED.getId()));
        work.setWorkStatus(publishedStatus);
        workRepository.save(work);
    }

}
