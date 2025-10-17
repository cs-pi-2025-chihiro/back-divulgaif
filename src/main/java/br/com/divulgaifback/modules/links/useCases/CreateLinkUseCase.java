package br.com.divulgaifback.modules.links.useCases;

import br.com.divulgaifback.modules.links.dtos.CreateLinkDTO;
import br.com.divulgaifback.modules.links.entities.Link;
import br.com.divulgaifback.modules.links.repositories.LinkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CreateLinkUseCase {

    @Autowired
    private LinkRepository linkRepository;

    public Link execute(CreateLinkDTO createLinkDTO) {
        Link link = Link.builder()
                .title(createLinkDTO.getTitle())
                .url(createLinkDTO.getUrl())
                .userId(createLinkDTO.getUserId())
                .build();

        return linkRepository.save(link);
    }
}