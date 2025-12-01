package br.com.divulgaifback.modules.users.useCases.user.create;

import br.com.divulgaifback.common.constants.SuapConstants;
import br.com.divulgaifback.common.exceptions.custom.DuplicateException;
import br.com.divulgaifback.common.exceptions.custom.NotFoundException;
import br.com.divulgaifback.modules.users.entities.Author;
import br.com.divulgaifback.modules.users.entities.Role;
import br.com.divulgaifback.modules.users.entities.User;
import br.com.divulgaifback.modules.users.entities.enums.RoleEnum;
import br.com.divulgaifback.modules.users.repositories.AuthorRepository;
import br.com.divulgaifback.modules.users.repositories.RoleRepository;
import br.com.divulgaifback.modules.users.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CreateUserUseCase {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final CreateUserResponse createUserResponse;
    private final AuthorRepository authorRepository;

    @Transactional
    public CreateUserResponse execute(CreateUserRequest request) {
        validateDependencies(request);

        User user = CreateUserRequest.toDomain(request);
        if (Objects.nonNull(request.password())) user.setPassword(passwordEncoder.encode(request.password()));

        determineUserRole(request, user);

        User savedUser = userRepository.save(user);
        determineAuthorship(request, savedUser);

        return createUserResponse.toPresentation(savedUser);
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

    private void determineUserRole(CreateUserRequest request, User user) {
        if (Objects.equals(request.userType(), SuapConstants.STUDENT_SUAP_TYPE)) {
            Role isStudent = roleRepository.findByName(RoleEnum.IS_STUDENT.name()).orElseThrow(() -> NotFoundException.with(Role.class, "name", RoleEnum.IS_STUDENT.name()));
            user.getRoles().add(isStudent);
        } else if (Objects.equals(request.userType(), SuapConstants.TEACHER_SUAP_TYPE)) {
            Role isTeacher = roleRepository.findByName(RoleEnum.IS_TEACHER.name()).orElseThrow(() -> NotFoundException.with(Role.class, "name", RoleEnum.IS_TEACHER.name()));
            user.getRoles().add(isTeacher);
        }
    }

    private void determineAuthorship(CreateUserRequest request, User user) {
        Optional<Author> author = authorRepository.findByEmail(request.email())
                .or(() -> Optional.ofNullable(request.secondaryEmail())
                        .flatMap(authorRepository::findByEmail));

        if (author.isEmpty()) return;

        authorRepository.updateAuthorsUserId(author.get().getId(), user);
    }
}
