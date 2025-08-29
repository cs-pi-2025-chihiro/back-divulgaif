package br.com.divulgaifback.modules.works.useCases.work.edit;

import jakarta.validation.constraints.*;
import org.hibernate.validator.constraints.URL;
import java.util.List;
import jakarta.validation.Valid;

public record EditWorkRequest(
        @NotNull(message = "{editwork.id.required}") Integer id,
        @NotBlank(message = "{editwork.title.required}") @Size(max = 255) String title,
        @NotBlank(message = "{editwork.description.required}") String description,
        @NotBlank(message = "{editwork.content.required}") String content,
        @NotBlank(message = "{editwork.principalLink.required}") @URL String principalLink,
        @NotBlank(message = "{editwork.metaTag.required}") String metaTag,
        @NotBlank(message = "{editwork.imageUrl.required}") @URL String imageUrl,
        @NotNull(message = "{editwork.teacherId.required}") Integer teacherId,
        @Valid List<Integer> studentIds,
        @Valid List<AuthorRequest> newAuthors,
        @Valid List<LabelRequest> workLabels,
        @Valid List<LinkRequest> workLinks,
        @NotBlank(message = "{editwork.workType.required}") String workType,
        @NotBlank(message = "{editwork.workStatus.required}") String workStatus
) {
    public record AuthorRequest(
            @NotBlank(message = "{editwork.authorequest.name.required}") String name,
            @NotBlank(message = "{editwork.authorequest.email.required}") @Email String email
    ) {}

    public record LabelRequest(
            @NotBlank(message = "{editwork.labelrequest.name.required}") String name,
            @NotBlank(message = "{editwork.labelrequest.color.required}") String color
    ) {}

    public record LinkRequest(
            String name,
            @NotBlank(message = "{editwork.linkrequest.url.required}") @URL String url,
            String description
    ) {}
}
