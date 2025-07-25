package br.com.divulgaifback.modules.works.useCases.author.create;

import br.com.divulgaifback.modules.users.entities.Author;
import lombok.Data;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Data
@Component
public class CreateAuthorResponse {
    public Integer id;
    public String name;
    public String email;
    public String type;

    @Autowired
    private ModelMapper modelMapper;

    public CreateAuthorResponse toPresentation(Author author) {
        return modelMapper.map(author, CreateAuthorResponse.class);
    }
}