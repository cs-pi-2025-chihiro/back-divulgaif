package br.com.divulgaifback.modules.users.controllers;

import br.com.divulgaifback.modules.users.useCases.user.create.CreateUserRequest;
import br.com.divulgaifback.modules.users.useCases.user.create.CreateUserResponse;
import br.com.divulgaifback.modules.users.useCases.user.create.CreateUserUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final CreateUserUseCase createUserUseCase;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateUserResponse createUser(@Valid @RequestBody CreateUserRequest request) {
        return this.createUserUseCase.execute(request);
    }
}
