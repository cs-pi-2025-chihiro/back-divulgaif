package br.com.divulgaifback.modules.works.useCases.author.update;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateAuthorRequest {
    @NotBlank
    private String name;
    private String email;
    private String type;
}

