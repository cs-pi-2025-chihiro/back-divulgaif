package br.com.divulgaifback.modules.works.useCases.work.update;

import br.com.divulgaifback.modules.works.entities.Work;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

import java.util.List;

public record UpdateWorkRequest(
        @NotBlank(message = "{creatework.title.required}") @Size(max = 255) String title,
        String description,
        String content,
        String principalLink,
        String metaTag,
        String imageUrl,
        Integer teacherId,
        @Valid List<Integer> studentIds,
        @Valid List<AuthorRequest> newAuthors,
        @Valid List<LabelRequest> workLabels,
        @Valid List<LinkRequest> workLinks,
        @NotBlank(message = "{creatework.workType.required}") String workType,
        String workStatus
) {
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

    public static Work toDomain(UpdateWorkRequest request) {
        Work work = new Work();
        work.setTitle(request.title);
        work.setDescription(request.description);
        work.setContent(request.content);
        work.setPrincipalLink(request.principalLink);
        work.setMetaTag(request.metaTag);
        work.setImageUrl(request.imageUrl);
        return work;
    }
}