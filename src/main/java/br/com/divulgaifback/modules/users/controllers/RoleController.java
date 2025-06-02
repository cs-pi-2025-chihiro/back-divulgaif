package br.com.divulgaifback.modules.users.controllers;

import br.com.divulgaifback.modules.users.useCases.role.LinkUserRoleRequest;
import br.com.divulgaifback.modules.users.useCases.role.LinkUserRoleUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/roles")
public class RoleController {
    private final LinkUserRoleUseCase linkUserRoleUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public void link(@Valid @RequestBody LinkUserRoleRequest request) {
        linkUserRoleUseCase.execute(request);
    }
}
