package br.com.divulgaifback.modules.works.useCases.work.requestChanges;

import br.com.divulgaifback.modules.works.entities.Work;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.URL;

import java.util.List;
import java.util.Objects;

public record RequestChangesRequest(
        @NotBlank(message = "{requestChanges.message.required}") String feedbackMessage,
        @Size(max = 255) String title,
        String description,
        String content,
        String principalLink,
        String metaTag,
        String imageUrl,
        @Valid List<AuthorIdRequest> authors,
        @Valid List<AuthorRequest> newAuthors,
        @Valid List<LabelRequest> workLabels,
        @Valid List<LinkRequest> workLinks,
        String workType
) {
    public record AuthorIdRequest(
            @NotNull Integer id
    ) {}

    public record AuthorRequest(
            @NotBlank(message = "{creatework.authorequest.name.required}") String name,
            @NotBlank(message = "{creatework.authorequest.email.required}") @Email String email
    ) {}

    public record LabelRequest(
            @NotBlank(message = "{creatework.labelrequest.name.required}") String name,
            @NotBlank(message = "{creatework.labelrequest.color.required}") String color
    ) {}

    public record LinkRequest(
            String name,
            @NotBlank(message = "{creatework.linkrequest.url.required}") @URL String url,
            String description
    ) {}

    public static Work toDomain(RequestChangesRequest request, Work existingWork) {
        if (Objects.nonNull(request.title())) existingWork.setTitle(request.title());
        if (Objects.nonNull(request.description())) existingWork.setDescription(request.description());
        if (Objects.nonNull(request.content())) existingWork.setContent(request.content());
        if (Objects.nonNull(request.principalLink())) existingWork.setPrincipalLink(request.principalLink());
        if (Objects.nonNull(request.metaTag())) existingWork.setMetaTag(request.metaTag());
        if (Objects.nonNull(request.imageUrl())) existingWork.setImageUrl(request.imageUrl());
        return existingWork;
    }
}
