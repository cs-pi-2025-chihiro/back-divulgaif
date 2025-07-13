package br.com.divulgaifback.modules.works.useCases.author.list;

import br.com.divulgaifback.modules.users.entities.Author;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ListAuthorsResponse {
    public Integer id;
    public String name;
    public String email;
    public Integer userId;

    @Autowired
    private ModelMapper modelMapper;

    public ListAuthorsResponse toPresentation(Author author) {
        return modelMapper.map(author, ListAuthorsResponse.class);
    }
}
