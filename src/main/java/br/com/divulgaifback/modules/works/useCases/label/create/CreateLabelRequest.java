package br.com.divulgaifback.modules.works.useCases.label.create;

import br.com.divulgaifback.modules.works.entities.Label;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateLabelRequest(
    @NotBlank(message = "Name is required") String name,
    @NotNull(message = "workId is required") Integer workId
) {
    public static Label toDomain(CreateLabelRequest request) {
        Label label = new Label();
        label.setName(request.name);
        return label;
    }
}
