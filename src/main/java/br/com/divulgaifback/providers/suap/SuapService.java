package br.com.divulgaifback.providers.suap;

import br.com.divulgaifback.common.exceptions.custom.UnauthorizedException;
import br.com.divulgaifback.common.utils.Constants;
import br.com.divulgaifback.modules.auth.useCases.oauthLogin.OauthLoginRequest.*;
import br.com.divulgaifback.modules.auth.useCases.oauthLogin.OauthLoginRequest;
import br.com.divulgaifback.modules.users.entities.User;
import br.com.divulgaifback.modules.users.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SuapService {
    private final UserRepository userRepository;

    public User suapOauthLogin(OauthLoginRequest request) {
        validateSuapData(request.userData());
        return findSuapUser(request.userData());
    }

    public User findSuapUser(UserData suapData) {
        Optional<User> userByRa = userRepository.findByRa(suapData.identificacao());
        Optional<User> userByEmail = userByRa.isPresent() ? userByRa : userRepository.findByEmail(suapData.email());

        if (userByEmail.isEmpty()) throw new UnauthorizedException();
        return userByEmail.get();
    }

    public void validateSuapData(OauthLoginRequest.UserData data) {
        if (!data.identificacao().matches("\\d{8,15}")) {
            throw new UnauthorizedException();
        }

        if (!data.email().toLowerCase().contains("ifpr")) {
            throw new UnauthorizedException();
        }

        if (!(Objects.equals(data.tipoUsuario(), Constants.STUDENT_SUAP_TYPE)
                || Objects.equals(data.tipoUsuario(), Constants.TEACHER_SUAP_TYPE))) {
            throw new UnauthorizedException();
        }
    }
}