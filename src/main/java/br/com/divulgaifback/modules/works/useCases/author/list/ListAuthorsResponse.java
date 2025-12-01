package br.com.divulgaifback.modules.works.useCases.author.list;

import br.com.divulgaifback.modules.users.entities.Author;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@Data
public class ListAuthorsResponse {
    public Integer id;
    public String name;
    public String email;
    public Integer userId;

    @Autowired
    @JsonIgnore
    private ModelMapper modelMapper;

    public ListAuthorsResponse toPresentation(Author author) {
        ListAuthorsResponse response = new ListAuthorsResponse();
        response.setId(author.getId());
        response.setName(author.getName());
        response.setEmail(author.getEmail());
        response.setUserId(Objects.nonNull(author.getUser()) ? author.getUser().getId() : null);
        return modelMapper.map(response, ListAuthorsResponse.class);
    }
}
