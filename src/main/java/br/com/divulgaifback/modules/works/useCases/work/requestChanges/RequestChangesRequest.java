package br.com.divulgaifback.modules.works.useCases.work.requestChanges;

import br.com.divulgaifback.modules.works.useCases.work.update.UpdateWorkRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public record RequestChangesRequest(
        @NotBlank(message = "{requestChanges.message.required}") String message,
        @NotNull(message = "{requestChanges.workId.required}") @Positive Integer workId,
        @NotNull(message = "{requestChanges.userId.required}") @Positive Integer userId,
        @Size(max = 255) String title,
        String description,
        String content,
        String principalLink,
        String metaTag,
        String imageUrl,
        Integer teacherId,
        @Valid List<UpdateWorkRequest.AuthorIdRequest> authors,
        @Valid List<UpdateWorkRequest.AuthorRequest> newAuthors,
        @Valid List<UpdateWorkRequest.LabelRequest> workLabels,
        @Valid List<UpdateWorkRequest.LinkRequest> workLinks,
        String workType
) {
}
