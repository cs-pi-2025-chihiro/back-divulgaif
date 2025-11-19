package br.com.divulgaifback.modules.links.useCases;

import br.com.divulgaifback.modules.links.dtos.UpdateLinkDTO;
import br.com.divulgaifback.modules.links.entities.Link;
import br.com.divulgaifback.modules.links.repositories.LinkRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UpdateLinkUseCase {

    @Autowired
    private LinkRepository linkRepository;

    public Optional<Link> execute(Integer linkId, UpdateLinkDTO updateLinkDTO) {
        return linkRepository.findById(linkId).map(existingLink -> {
            existingLink.setTitle(updateLinkDTO.getTitle());
            existingLink.setUrl(updateLinkDTO.getUrl());
            return linkRepository.save(existingLink);
        });
    }
}