package br.com.divulgaifback.modules.works.useCases.history.create;

import br.com.divulgaifback.modules.works.entities.History;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CreateHistoryResponse {
    public Integer id;
    public String message;

    @Autowired
    private ModelMapper modelMapper;

    public CreateHistoryResponse toPresentation(History history) {
        return modelMapper.map(history, CreateHistoryResponse.class);
    }
}
