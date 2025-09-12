package br.com.divulgaifback.modules.works.useCases.work.requestChanges;

import br.com.divulgaifback.common.constants.AuthorConstants;
import br.com.divulgaifback.common.exceptions.custom.NotFoundException;
import br.com.divulgaifback.common.exceptions.custom.ValidationException;
import br.com.divulgaifback.modules.users.entities.Author;
import br.com.divulgaifback.modules.users.repositories.AuthorRepository;
import br.com.divulgaifback.modules.works.entities.*;
import br.com.divulgaifback.modules.works.entities.enums.WorkStatusEnum;
import br.com.divulgaifback.modules.works.repositories.*;
import com.querydsl.core.util.StringUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RequestChangesUseCase {
    private final WorkRepository workRepository;
    private final WorkTypeRepository workTypeRepository;
    private final WorkStatusRepository workStatusRepository;
    private final AuthorRepository authorRepository;
    private final LinkRepository linkRepository;
    private final LabelRepository labelRepository;

    @Secured({"IS_ADMIN", "IS_TEACHER"})
    @Transactional
    public void execute(RequestChangesRequest request, Integer workId) {
        Work work = workRepository.findById(workId).orElseThrow(() -> NotFoundException.with(Work.class, "id", workId));
        validateApproval(work);
        RequestChangesRequest.toDomain(request, work);

        if (Objects.nonNull(request.workType())) addType(work, request.workType());
        addPendingChangesStatus(work);

        handleAuthors(work, request);
        handleLabels(work, request.workLabels());
        handleLinks(work, request.workLinks());
    }

    private void validateApproval(Work work) {
        if (work.isApproved() || work.getWorkStatus().getName().equals(WorkStatusEnum.PUBLISHED.name()))
            throw new ValidationException("{requestChanges.work.alreadyapproved}");
    }

    private void addPendingChangesStatus(Work work) {
        String pendingChangesStatus = WorkStatusEnum.PENDING_CHANGES.name();
        WorkStatus status = workStatusRepository.findByName(pendingChangesStatus).orElseThrow(() -> NotFoundException.with(WorkStatus.class, "name", pendingChangesStatus));
        work.setWorkStatus(status);
    }

    private void addType(Work work, String workType) {
        WorkType type = workTypeRepository.findByName(workType).orElseThrow(() -> NotFoundException.with(WorkType.class, "name", workType));
        work.setWorkType(type);
    }

    private void handleAuthors(Work work, RequestChangesRequest request) {
        work.getAuthors().clear();
        if (hasNewAuthors(request)) handleNonDivulgaIfUsers(work, request.newAuthors());
        if (hasExistingAuthors(request)) handleExistingAuthors(work, request.authors());
    }

    private boolean hasNewAuthors(RequestChangesRequest request) {
        return Objects.nonNull(request.newAuthors()) && !request.newAuthors().isEmpty();
    }

    private boolean hasExistingAuthors(RequestChangesRequest request) {
        return Objects.nonNull(request.authors()) && !request.authors().isEmpty();
    }

    private void handleNonDivulgaIfUsers(Work work, List<RequestChangesRequest.AuthorRequest> newAuthors) {
        if (Objects.isNull(newAuthors) || newAuthors.isEmpty()) return;

        newAuthors.forEach(newAuthor -> {
            String name = newAuthor.name().trim();
            String email = newAuthor.email().trim().toLowerCase();

            if (workContainsAuthorEmail(work, email)) return;

            List<Author> existingAuthors = authorRepository.findAllByEmail(email);

            if (!existingAuthors.isEmpty()) work.addAuthor(existingAuthors.getFirst());

            else {
                Author author = new Author();
                author.setName(name);
                author.setEmail(email);
                author.setType(AuthorConstants.UNREGISTERED_AUTHOR);

                authorRepository.save(author);
                work.addAuthor(author);
            }
        });
    }

    private void handleExistingAuthors(Work work, List<RequestChangesRequest.AuthorIdRequest> authorIds) {
        authorIds.forEach(authorIdRequest -> {
            Author author = authorRepository.findById(authorIdRequest.id()).orElseThrow(() -> NotFoundException.with(Author.class, "id", authorIdRequest.id()));

            if (!workContainsAuthorEmail(work, author.getEmail()))
                work.addAuthor(author);
        });
    }

    private boolean workContainsAuthorEmail(Work work, String email) {
        if (StringUtils.isNullOrEmpty(email)) return false;
        return work.getAuthors().stream()
                .anyMatch(a -> a.getEmail() != null && email.equalsIgnoreCase(a.getEmail()));
    }


    private void handleLabels(Work work, List<RequestChangesRequest.LabelRequest> labels) {
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

    private void handleLinks(Work work, List<RequestChangesRequest.LinkRequest> links) {
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
}
