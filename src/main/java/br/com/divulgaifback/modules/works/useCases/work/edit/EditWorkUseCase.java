package br.com.divulgaifback.modules.works.useCases.work.edit;

import br.com.divulgaifback.common.exceptions.custom.NotFoundException;
import br.com.divulgaifback.modules.works.entities.Work;
import br.com.divulgaifback.modules.works.repositories.WorkRepository;
import br.com.divulgaifback.modules.works.repositories.WorkTypeRepository;
import br.com.divulgaifback.modules.works.repositories.WorkStatusRepository;
import br.com.divulgaifback.modules.users.repositories.UserRepository;
import br.com.divulgaifback.modules.works.entities.WorkType;
import br.com.divulgaifback.modules.works.entities.WorkStatus;
import br.com.divulgaifback.modules.users.entities.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EditWorkUseCase {
    private final WorkRepository workRepository;
    private final WorkTypeRepository workTypeRepository;
    private final WorkStatusRepository workStatusRepository;
    private final UserRepository userRepository;

    @Transactional
    public EditWorkResponse execute(EditWorkRequest request) {
        Work work = workRepository.findById(request.id()).orElseThrow(() -> NotFoundException.with(Work.class, "id", request.id()));

        WorkType workType = workTypeRepository.findByName(request.workType()).orElseThrow(() -> NotFoundException.with(WorkType.class, "name", request.workType()));
        WorkStatus workStatus = workStatusRepository.findByName(request.workStatus()).orElseThrow(() -> NotFoundException.with(WorkStatus.class, "name", request.workStatus()));
        User teacher = userRepository.findById(request.teacherId()).orElseThrow(() -> NotFoundException.with(User.class, "id", request.teacherId()));

        work.setTitle(request.title());
        work.setDescription(request.description());
        work.setContent(request.content());
        work.setPrincipalLink(request.principalLink());
        work.setMetaTag(request.metaTag());
        work.setImageUrl(request.imageUrl());
        work.setTeacher(teacher);
        work.setWorkType(workType);
        work.setWorkStatus(workStatus);

        workRepository.save(work);

        return new EditWorkResponse().toPresentation(work);
    }
}
