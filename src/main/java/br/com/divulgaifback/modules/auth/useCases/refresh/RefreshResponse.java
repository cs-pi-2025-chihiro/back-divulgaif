package br.com.divulgaifback.modules.auth.useCases.refresh;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RefreshResponse {
    public String accessToken;

    @Autowired
    private ModelMapper modelMapper;

    public RefreshResponse toPresentation(String accessToken) {
        RefreshResponse response = new RefreshResponse();
        response.accessToken = modelMapper.map(accessToken, String.class);
        return response;
    }
}

