package br.com.divulgaifback.modules.works.useCases.author.update;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UpdateAuthorResponse {
    private String id;
    private String name;
    private String email;
    private String type;

}

