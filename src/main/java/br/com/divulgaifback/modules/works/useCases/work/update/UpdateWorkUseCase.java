package br.com.divulgaifback.modules.works.useCases.work.update;

import br.com.divulgaifback.common.constants.AuthorConstants;
import br.com.divulgaifback.common.constants.WorkConstants;
import br.com.divulgaifback.common.exceptions.custom.ForbiddenException;
import br.com.divulgaifback.common.exceptions.custom.NotFoundException;
import br.com.divulgaifback.modules.auth.services.AuthService;
import br.com.divulgaifback.modules.users.entities.Author;
import br.com.divulgaifback.modules.users.entities.User;
import br.com.divulgaifback.modules.users.repositories.AuthorRepository;
import br.com.divulgaifback.modules.users.repositories.UserRepository;
import br.com.divulgaifback.modules.works.entities.*;
import br.com.divulgaifback.modules.works.repositories.*;
import com.querydsl.core.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import br.com.divulgaifback.modules.works.useCases.work.update.UpdateWorkRequest.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UpdateWorkUseCase {
    private final UpdateWorkResponse workResponse;
    private final UserRepository userRepository;
    private final WorkStatusRepository workStatusRepository;
    private final WorkTypeRepository workTypeRepository;
    private final WorkRepository workRepository;
    private final LabelRepository labelRepository;
    private final LinkRepository linkRepository;
    private final AuthorRepository authorRepository;

    @Transactional
    public UpdateWorkResponse execute(UpdateWorkRequest request, Integer id) {
        Work work = workRepository.findById(id).orElseThrow(() -> new NotFoundException("Work not found"));
        
        validateWorkOwnership(work);

        updateWorkFromRequest(work, request);
        handleAuthors(work, request);
        handleLabels(work, request.workLabels());
        handleLinks(work, request.workLinks());
        addStatus(work, request.workStatus());
        addType(work, request.workType());

        workRepository.save(work);
        return workResponse.toPresentation(work);
    }

    private void updateWorkFromRequest(Work work, UpdateWorkRequest request) {
        work.setTitle(request.title());
        work.setDescription(request.description());
        work.setContent(request.content());
        work.setPrincipalLink(request.principalLink());
        work.setMetaTag(request.metaTag());
        work.setImageUrl(request.imageUrl());
    }

    private void addStatus(Work work, String workStatus) {
        String statusName = StringUtils.isNullOrEmpty(workStatus) ? WorkConstants.DRAFT_STATUS : workStatus;
        WorkStatus status = workStatusRepository.findByName(statusName).orElseThrow(() -> NotFoundException.with(WorkStatus.class, "name", statusName));
        work.setWorkStatus(status);
    }

    private void addType(Work work, String workType) {
        WorkType type = workTypeRepository.findByName(workType).orElseThrow(() -> NotFoundException.with(WorkType.class, "name", workType));
        work.setWorkType(type);
    }

    private void handleAuthors(Work work, UpdateWorkRequest request) {
        work.getAuthors().clear();

        if (hasNewAuthors(request)) {
            handleNonDivulgaIfUsers(work, request.newAuthors());
        }
        if (hasStudents(request)) {
            handleDivulgaIfStudents(work, request.studentIds());
        }

        addMainAuthor(work);
    }

    private void addMainAuthor(Work work) {
        Author author = convertUserToAuthor(AuthService.getUserFromToken());
        if (!workContainsAuthorEmail(work, author.getEmail())) {
            work.addAuthor(author);
        }
    }
    
    private boolean hasNewAuthors(UpdateWorkRequest request) {
        return Objects.nonNull(request.newAuthors()) && !request.newAuthors().isEmpty();
    }

    private boolean hasStudents(UpdateWorkRequest request) {
        return Objects.nonNull(request.studentIds()) && !request.studentIds().isEmpty();
    }

    private void handleNonDivulgaIfUsers(Work work, List<AuthorRequest> newAuthors) {
        if (Objects.isNull(newAuthors) || newAuthors.isEmpty()) return;

        newAuthors.forEach(newAuthor -> {
            String name = newAuthor.name().trim();
            String email = newAuthor.email().trim().toLowerCase();

            if (workContainsAuthorEmail(work, email)) {
                return;
            }

            List<Author> existingAuthors = authorRepository.findAllByEmail(email);

            if (!existingAuthors.isEmpty()) {
                work.addAuthor(existingAuthors.get(0));
            } else {
                Author author = new Author();
                author.setName(name);
                author.setEmail(email);
                author.setType(AuthorConstants.UNREGISTERED_AUTHOR);

                authorRepository.save(author);
                work.addAuthor(author);
            }
        });
    }

    private void handleDivulgaIfStudents(Work work, List<Integer> studentIds) {
        studentIds.forEach(studentId -> {
            User student = userRepository.findById(studentId).orElseThrow(() -> NotFoundException.with(User.class, "id", studentId));
            Author studentAuthor = convertUserToAuthor(student);
            if (!workContainsAuthorEmail(work, studentAuthor.getEmail())) {
                work.addAuthor(studentAuthor);
            }
        });
    }

    private Author convertUserToAuthor(User user) {
        String userEmail = user.getEmail().toLowerCase();
        List<Author> existingAuthors = authorRepository.findAllByEmail(userEmail);

        if (!existingAuthors.isEmpty()) {
            Optional<Author> linkedToUser = existingAuthors.stream()
                    .filter(a -> a.getUser() != null && a.getUser().getId().equals(user.getId()))
                    .findFirst();

            if (linkedToUser.isPresent()) {
                return linkedToUser.get();
            }

            Author preferred = existingAuthors.stream()
                    .filter(a -> a.getUser() != null)
                    .findFirst()
                    .orElse(existingAuthors.get(0));

            if (preferred.getUser() == null) {
                preferred.setUser(user);
                preferred.setType(AuthorConstants.REGISTERED_AUTHOR);
                authorRepository.save(preferred);
            }
            return preferred;
        }

        Author author = new Author();
        author.setName(user.getName());
        author.setEmail(userEmail);
        author.setType(AuthorConstants.REGISTERED_AUTHOR);
        author.setUser(user);
        authorRepository.save(author);
        return author;
    }

    private boolean workContainsAuthorEmail(Work work, String email) {
        if (StringUtils.isNullOrEmpty(email)) return false;
        return work.getAuthors().stream()
                   .anyMatch(a -> a.getEmail() != null && email.equalsIgnoreCase(a.getEmail()));
    }

    private void handleLabels(Work work, List<LabelRequest> labels) {
        work.getLabels().clear();
        if (Objects.isNull(labels) || labels.isEmpty()) return;

        labels.forEach(label -> {
            Optional<Label> existentLabel = labelRepository.findByName(label.name());
            if (existentLabel.isPresent()) {
                work.addLabel(existentLabel.get());
                return;
            }

            Label newLabel = new Label();
            newLabel.setName(label.name());
            newLabel.setColor(label.color());
            Label savedLabel = labelRepository.save(newLabel);
            work.addLabel(savedLabel);
        });
    }

    private void handleLinks(Work work, List<LinkRequest> links) {
        work.getLinks().clear();
        if (Objects.isNull(links) || links.isEmpty()) return;

        links.forEach(link -> {
            Optional<Link> existentLink = linkRepository.findByUrl(link.url());
            if (existentLink.isPresent()) {
                work.addLink(existentLink.get());
                return;
            }

            Link newLink = new Link();
            newLink.setName(link.name());
            newLink.setUrl(link.url());
            newLink.setDescription(link.description());
            Link savedLink = linkRepository.save(newLink);
            work.addLink(savedLink);
        });
    }

    private void validateWorkOwnership(Work work) {
        User currentUser = AuthService.getUserFromToken();
        
        boolean isAuthor = work.getAuthors().stream()
                .anyMatch(author -> author.getUser() != null && 
                         author.getUser().getId().equals(currentUser.getId()));
        
        if (!isAuthor) {
            throw new ForbiddenException("You can only edit works you have authored");
        }
    }
}