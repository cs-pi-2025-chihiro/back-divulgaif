package br.com.divulgaifback.modules.works.useCases.link.list;

import br.com.divulgaifback.modules.works.entities.Link;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ListLinksResponse {
    public Integer id;
    public String url;

    @Autowired
    private ModelMapper modelMapper;

    public ListLinksResponse toPresentation(Link link) {
        return modelMapper.map(link, ListLinksResponse.class);
    }
}
