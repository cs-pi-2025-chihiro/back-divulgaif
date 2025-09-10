package br.com.divulgaifback.modules.users.useCases.role;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record LinkUserRoleRequest(
        @NotNull(message = "{linkrole.userId.required}") @Positive Integer userId,
        @Valid @NotEmpty(message = "{linkrole.roleIds.required}") List<Integer> roleIds
) {}
