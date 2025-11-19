package br.com.divulgaifback.modules.links.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateLinkDTO {

    @NotBlank
    private String title;

    @NotBlank
    private String url;

    private Integer userId;
}