package br.com.divulgaifback.modules.works.useCases.work.create;

import br.com.divulgaifback.modules.works.entities.Work;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record CreateWorkRequest(
        @NotBlank(message = "title is required") String title,
        String description,
        String content,
        String principalLink,
        String metaTag,
        String imageUrl,
        Integer teacherId,
        List<Integer> studentIds,
        List<AuthorRequest> newAuthors,
        List<LabelRequest> workLabels,
        List<LinkRequest> workLinks,
        @NotBlank(message = "workType is required") String workType,
        String workStatus
) {
    public record AuthorRequest(
            @NotBlank(message = "name is required") String name,
            @NotBlank(message = "email is required") String email
    ) {}

    public record LabelRequest(
            @NotBlank(message = "name is required") String name,
            @NotBlank(message = "color is required") String color
    ) {}

    public record LinkRequest(
            @NotBlank(message = "name is required") String name,
            @NotBlank(message = "url is required") String url,
            String description
    ) {}

    public static Work toDomain(CreateWorkRequest request) {
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

