package br.com.divulgaifback.modules.works.useCases.label.create;

import br.com.divulgaifback.modules.works.entities.Label;
import jakarta.validation.constraints.NotBlank;

public record CreateLabelRequest(
    @NotBlank(message = "{label.name.required}") String name,
    Integer workId
) {
    public static Label toDomain(CreateLabelRequest request) {
        Label label = new Label();
        label.setName(request.name);
        return label;
    }
}
