package br.com.divulgaifback.modules.works.useCases.history.create;

import br.com.divulgaifback.common.exceptions.custom.EmailException;
import br.com.divulgaifback.common.exceptions.custom.NotFoundException;
import br.com.divulgaifback.common.exceptions.custom.ValidationException;
import br.com.divulgaifback.common.services.EmailService;
import br.com.divulgaifback.modules.users.entities.Author;
import br.com.divulgaifback.modules.users.entities.User;
import br.com.divulgaifback.modules.users.entities.enums.RoleEnum;
import br.com.divulgaifback.modules.users.repositories.UserRepository;
import br.com.divulgaifback.modules.works.entities.History;
import br.com.divulgaifback.modules.works.entities.Work;
import br.com.divulgaifback.modules.works.entities.enums.HistoryStatusEnum;
import br.com.divulgaifback.modules.works.entities.enums.WorkStatusEnum;
import br.com.divulgaifback.modules.works.repositories.HistoryRepository;
import br.com.divulgaifback.modules.works.repositories.WorkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CreateHistoryUseCase {
    private final UserRepository userRepository;
    private final WorkRepository workRepository;
    private final HistoryRepository historyRepository;
    private final CreateHistoryResponse createHistoryResponse;
    private final EmailService emailService;

    @Transactional
    public CreateHistoryResponse execute(CreateHistoryRequest request) {
        History history = CreateHistoryRequest.toDomain(request);
        User teacher = userRepository.findById(request.userId()).orElseThrow(() -> NotFoundException.with(User.class, "id", request.userId()));
        Work work = workRepository.findById(request.workId()).orElseThrow(() -> NotFoundException.with(Work.class, "id", request.workId())); // Fix: was User.class

        validateHistory(work);

        history.setTeacher(teacher);
        work.addHistory(history);
        history.setStatusId(HistoryStatusEnum.PENDING_CHANGES.id);

        for (Author author : work.getAuthors()) {
            if (Objects.isNull(author.getUser()) || !author.getUser().hasRole(RoleEnum.IS_STUDENT.getValue())) continue;
            try {
                User user = author.getUser();
                emailService.sendNewFeedbackAddedEmail(user.getEmail(), user.getName(), work.getTitle());
            } catch (Exception e) {
                throw new EmailException(e.getMessage());
            }
        }

        historyRepository.save(history);
        return createHistoryResponse.toPresentation(history);
    }

    private void validateHistory(Work work) {
        if (work.isApproved()) throw new ValidationException("{createhistory.work.alreadyapproved}");
    }
}
