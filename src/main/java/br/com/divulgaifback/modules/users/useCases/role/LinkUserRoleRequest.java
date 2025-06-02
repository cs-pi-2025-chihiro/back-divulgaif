package br.com.divulgaifback.modules.users.useCases.role;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record LinkUserRoleRequest(
        @NotNull(message = "userId is required") @Positive Integer userId,
        @Valid @NotEmpty(message = "roleIds are required") List<Integer> roleIds
) {}
