package br.com.divulgaifback.modules.works.useCases.link.create;

import br.com.divulgaifback.modules.works.entities.Link;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CreateLinkResponse {
    public Integer id;
    public String name;
    public String url;
    public String description;

    @Autowired
    private ModelMapper modelMapper;

    public CreateLinkResponse toPresentation(Link link) {
        return modelMapper.map(link, CreateLinkResponse.class);
    }
}
