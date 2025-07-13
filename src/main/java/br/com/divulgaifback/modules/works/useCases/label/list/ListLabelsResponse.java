package br.com.divulgaifback.modules.works.useCases.label.list;

import br.com.divulgaifback.modules.works.entities.Label;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ListLabelsResponse {
    public Integer id;
    public String name;

    @Autowired
    private ModelMapper modelMapper;

    public ListLabelsResponse toPresentation(Label label) {
        return modelMapper.map(label, ListLabelsResponse.class);
    }

}
