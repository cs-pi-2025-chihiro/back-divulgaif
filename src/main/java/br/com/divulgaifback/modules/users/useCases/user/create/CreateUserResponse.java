package br.com.divulgaifback.modules.users.useCases.user.create;

import br.com.divulgaifback.modules.users.entities.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
@Component
public class CreateUserResponse {
    public String accessToken;
    public String refreshToken;
    public GetCreatedUser user;

    @Autowired
    private ModelMapper modelMapper;

    public static class GetCreatedUser {
        public Integer id;
        public String name;
        public String email;
        public String cpf;
        public String bio;
        public String phone;
        public String avatarUrl;
        public Set<GetCreatedUserRole> roles;
    }

    public static class GetCreatedUserRole {
        public Integer id;
        public String name;
    }

    public CreateUserResponse toPresentation(String accessToken, String refreshToken, User user) {
        CreateUserResponse response = new CreateUserResponse();
        response.accessToken = accessToken;
        response.refreshToken = refreshToken;
        response.user = modelMapper.map(user, GetCreatedUser.class);
        return response;
    }
}
