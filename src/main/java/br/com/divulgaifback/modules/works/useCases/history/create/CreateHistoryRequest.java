package br.com.divulgaifback.modules.works.useCases.history.create;

import br.com.divulgaifback.modules.works.entities.History;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateHistoryRequest(
        @NotBlank(message = "{createhistory.message.required}") String message,
        @NotNull(message = "{createhistory.workId.required}") @Positive Integer workId,
        @NotNull(message = "{createhistory.userId.required}") @Positive Integer userId
) {
    public static History toDomain(CreateHistoryRequest request) {
        History history = new History();
        history.setMessage(request.message);
        return history;
    }
}
