package br.com.divulgaifback.modules.users.useCases.user.create;

import br.com.divulgaifback.modules.users.entities.User;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class CreateUserResponse {
    public Integer id;
    public String name;
    public String email;
    public String cpf;
    public String bio;
    public String phone;
    public String avatarUrl;

    @Autowired
    private ModelMapper modelMapper;

    public CreateUserResponse toPresentation(User user) {
        return modelMapper.map(user, CreateUserResponse.class);
    }
}
