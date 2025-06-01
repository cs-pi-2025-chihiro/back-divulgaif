package br.com.divulgaifback.modules.users.useCases.user.create;

import br.com.divulgaifback.common.exceptions.custom.DuplicateException;
import br.com.divulgaifback.common.exceptions.custom.NotFoundException;
import br.com.divulgaifback.modules.users.entities.Role;
import br.com.divulgaifback.modules.users.entities.User;
import br.com.divulgaifback.modules.users.entities.enums.RoleEnum;
import br.com.divulgaifback.modules.users.repositories.RoleRepository;
import br.com.divulgaifback.modules.users.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CreateUserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final CreateUserResponse createUserResponse;

    @Transactional
    public CreateUserResponse execute(CreateUserRequest request) {
        validateDependencies(request);
        User user = CreateUserRequest.toDomain(request);
        user.setPassword(passwordEncoder.encode(request.password()));

        Role isStudent = roleRepository.findByName(RoleEnum.IS_STUDENT.name())
                .orElseThrow(() -> NotFoundException
                        .with(Role.class, "name", RoleEnum.IS_STUDENT.name()));
        user.getRoles().add(isStudent);

        userRepository.save(user);

        return createUserResponse.toPresentation(user);
    }

    private void validateDependencies(CreateUserRequest request) {
        validateUniqueEmail(request.email());
        validateUniqueCpf(request.cpf());
        validateUniqueRa(request.ra());
    }

    private void validateUniqueEmail(String email) {
        if (this.userRepository.findByEmail(email).isPresent()) {
            throw DuplicateException.with(User.class, "email", email);
        }
    }

    private void validateUniqueCpf(String cpf) {
        if (Objects.nonNull(cpf)) {
            if (this.userRepository.findByCpf(cpf).isPresent()) {
                throw DuplicateException.with(User.class, "cpf", cpf);
            }
        }
    }

    private void validateUniqueRa(String ra) {
        if (Objects.nonNull(ra)) {
            if (this.userRepository.findByRa(ra).isPresent()) {
                throw DuplicateException.with(User.class, "ra", ra);
            }
        }
    }
}
