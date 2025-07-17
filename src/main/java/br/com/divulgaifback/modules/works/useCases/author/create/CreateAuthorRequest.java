package br.com.divulgaifback.modules.works.useCases.author.create;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateAuthorRequest(
    @NotBlank(message = "Name is required") String name,
    @Email @NotBlank(message = "Email is required") String email
) {}