package br.com.divulgaifback.modules.auth.useCases.login;

import br.com.divulgaifback.modules.users.entities.User;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class LoginResponse {
    public String accessToken;
    public String refreshToken;
    public GetLoginUser user;

    @Autowired
    private ModelMapper modelMapper;

    public static class GetLoginUser {
        public Integer id;
        public String name;
        public String email;
        public String cpf;
        public Set<GetLoginRole> roles;
    }

    public static class GetLoginRole {
        public Integer id;
        public String name;
    }

    public LoginResponse toPresentation(String accessToken, String refreshToken, User user) {
        LoginResponse response = new LoginResponse();
        response.accessToken = accessToken;
        response.refreshToken = refreshToken;
        response.user = modelMapper.map(user, GetLoginUser.class);
        return response;
    }
}
