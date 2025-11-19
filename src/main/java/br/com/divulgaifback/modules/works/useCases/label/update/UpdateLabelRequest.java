package br.com.divulgaifback.modules.works.useCases.label.update;

import jakarta.validation.constraints.NotBlank;

public record UpdateLabelRequest(
    @NotBlank(message = "{label.name.required}") String name
) {
}