package br.com.divulgaifback.modules.works.useCases.work.create;

import br.com.divulgaifback.common.exceptions.custom.NotFoundException;
import br.com.divulgaifback.common.exceptions.custom.ValidationException;
import br.com.divulgaifback.common.utils.Constants;
import br.com.divulgaifback.modules.auth.services.AuthService;
import br.com.divulgaifback.modules.users.entities.Author;
import br.com.divulgaifback.modules.users.entities.User;
import br.com.divulgaifback.modules.users.repositories.AuthorRepository;
import br.com.divulgaifback.modules.users.repositories.UserRepository;
import br.com.divulgaifback.modules.works.entities.*;
import br.com.divulgaifback.modules.works.repositories.*;
import br.com.divulgaifback.modules.works.useCases.work.create.CreateWorkRequest.*;
import com.querydsl.core.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CreateWorkUseCase {
    private final CreateWorkResponse workResponse;
    private final UserRepository userRepository;
    private final WorkStatusRepository workStatusRepository;
    private final WorkTypeRepository workTypeRepository;
    private final WorkRepository workRepository;
    private final LabelRepository labelRepository;
    private final LinkRepository linkRepository;
    private final AuthorRepository authorRepository;

    @Transactional
    public CreateWorkResponse execute(CreateWorkRequest request) {
        Work work = CreateWorkRequest.toDomain(request);

        handleAuthors(work, request);
        handleLabels(work, request.workLabels());
        handleLinks(work, request.workLinks());
        addStatus(work, request.workStatus());
        addType(work, request.workType());
        workRepository.save(work);
        return workResponse.toPresentation(work);
    }

    private void addStatus(Work work, String workStatus) {
        String statusName = StringUtils.isNullOrEmpty(workStatus) ? Constants.draftStatus : workStatus;
        WorkStatus status = workStatusRepository.findByName(statusName).orElseThrow(() -> NotFoundException.with(WorkStatus.class, "name", statusName));
        work.setWorkStatus(status);
    }

    private void addType(Work work, String workType) {
        WorkType type = workTypeRepository.findByName(workType).orElseThrow(() -> NotFoundException.with(WorkType.class, "name", workType));
        work.setWorkType(type);
    }

    private void handleAuthors(Work work, CreateWorkRequest request) {
        addMainAuthor(work);
        if (hasStudents(request)) {
            handleDivulgaIfStudents(work, request.studentIds());
        } else {
            handleNonDivulgaIfUsers(work, request.newAuthors());
        }
    }

    private void addMainAuthor(Work work) {
        Author author = convertUserToAuthor(AuthService.getUserFromToken());
        work.addAuthor(author);
    }

    private boolean hasStudents(CreateWorkRequest request) {
        return Objects.nonNull(request.studentIds()) && !request.studentIds().isEmpty();
    }

    private void handleNonDivulgaIfUsers(Work work, List<AuthorRequest> newAuthors) {
        if (Objects.isNull(newAuthors) || newAuthors.isEmpty()) return;

        newAuthors.forEach(newAuthor -> {
            Author author = new Author();
            String name = newAuthor.name();
            String email = newAuthor.email();

            if (StringUtils.isNullOrEmpty(name) || StringUtils.isNullOrEmpty(email)) {
                throw new ValidationException("An authors' name and email are required");
            }

            author.setName(name.trim());
            author.setEmail(email.trim());

            authorRepository.save(author);
            work.addAuthor(author);
        });
    }

    private void handleDivulgaIfStudents(Work work, List<Integer> studentIds) {
        studentIds.forEach(studentId -> {
            User student = userRepository.findById(studentId).orElseThrow(() -> NotFoundException.with(User.class, "id", studentId));
            Author studentAuthor = convertUserToAuthor(student);
            work.addAuthor(studentAuthor);
        });
    }

    private Author convertUserToAuthor(User student) {
        Author author = new Author();
        author.setName(student.getName());
        author.setEmail(student.getEmail());
        author.setType(Constants.registeredAuthor);
        author.setUser(student);
        authorRepository.save(author);
        return author;
    }

    private void handleLabels(Work work, List<LabelRequest> labels) {
        if (Objects.isNull(labels) || labels.isEmpty()) return;

        labels.forEach(label -> {
            Optional<Label> existentLabel = labelRepository.findByName(label.name());
            if (existentLabel.isPresent()) {
                work.addLabel(existentLabel.get());
                return;
            }

            Label newLabel = new Label();
            String name = label.name();
            String color = label.color();

            if (StringUtils.isNullOrEmpty(name) || StringUtils.isNullOrEmpty(color)) {
                throw new ValidationException("A label's name and color are required");
            }

            newLabel.setName(name);
            newLabel.setColor(color);

            Label savedLabel = labelRepository.save(newLabel);
            work.addLabel(savedLabel);
        });
    }

    private void handleLinks(Work work, List<LinkRequest> links) {
        if (Objects.isNull(links) || links.isEmpty()) return;

        links.forEach(link -> {
            Optional<Link> existentLink = linkRepository.findByUrl(link.url());
            if (existentLink.isPresent()) {
                work.addLink(existentLink.get());
                return;
            }

            Link newLink = new Link();
            String name = link.name();
            String url = link.url();
            String description = link.description();

            if (StringUtils.isNullOrEmpty(name) || StringUtils.isNullOrEmpty(url)) {
                throw new ValidationException("A link's name and url are required");
            }

            newLink.setName(name);
            newLink.setUrl(url);
            newLink.setDescription(description);

            Link savedLink = linkRepository.save(newLink);
            work.addLink(savedLink);
        });
    }
}
