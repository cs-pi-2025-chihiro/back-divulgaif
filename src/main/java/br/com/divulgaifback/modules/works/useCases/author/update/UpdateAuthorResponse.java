package br.com.divulgaifback.modules.works.useCases.author.update;

import br.com.divulgaifback.modules.users.entities.Author;
import lombok.Data;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Data
@Component
public class UpdateAuthorResponse {
    public Integer id;
    public String name;
    public String email;
    public String type;

    @Autowired
    private ModelMapper modelMapper;

    public UpdateAuthorResponse toPresentation(Author author) {
        return modelMapper.map(author, UpdateAuthorResponse.class);
    }
}

